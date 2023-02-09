package webserver;

import db.DataBase;
import model.User;
import org.junit.jupiter.api.Test;
import support.StubSocket;
import utils.FileIoUtils;
import webserver.handler.HandlerMapper;

import static org.assertj.core.api.Assertions.assertThat;

class RequestHandlerTest {

    @Test
    void socket_out() {
        // given
        final var socket = new StubSocket();
        final var handler = new RequestHandler(socket, new HandlerMapper());

        // when
        handler.run();

        // then
        assertThat(socket.output()).contains("HTTP/1.1 200 OK \r\n",
                "Content-Type: text/html; charset=utf-8 \r\n",
                "Content-Length: 11 \r\n",
                "\r\n",
                "Hello world");
    }

    @Test
    void index() {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /index.html HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket, new HandlerMapper());

        // when
        handler.run();

        // then
        assertThat(socket.output()).contains("HTTP/1.1 200 OK \r\n",
                "Content-Type: text/html; charset=utf-8 \r\n",
                "Content-Length: 7154 \r\n",
                "\r\n",
                new String(FileIoUtils.loadFileFromClasspath("templates/index.html")));
    }

    @Test
    void css() {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /css/styles.css HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: text/css,*/*;q=0.1 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket, new HandlerMapper());

        // when
        handler.run();

        // then
        assertThat(socket.output()).contains("HTTP/1.1 200 OK \r\n",
                "Content-Type: text/css; charset=utf-8 \r\n",
                "Content-Length: 7065 \r\n",
                "\r\n",
                new String(FileIoUtils.loadFileFromClasspath("static/css/styles.css")));
    }

    @Test
    void createUserGet() {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /user/create?userId=cu&password=password&name=%EC%9D%B4%EB%8F%99%EA%B7%9C&email=brainbackdoor%40gmail.com HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Accept: */* ",
                "",
                ""
        );

        final User user = new User(
                "cu",
                "password",
                "이동규",
                "brainbackdoor@gmail.com"
        );

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket, new HandlerMapper());

        // when
        handler.run();

        // then
        final User savedUser = DataBase.findUserById("cu");
        assertThat(user.getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(user.getPassword()).isEqualTo(savedUser.getPassword());
        assertThat(user.getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(user.getName()).isEqualTo(savedUser.getName());
    }

    @Test
    void createUserPost() {
        // given
        final String httpRequest = String.join("\r\n",
                "POST /user/create HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: 92 ",
                "Content-Type: application/x-www-form-urlencoded ",
                "Accept: */* ",
                "",
                "userId=cu&password=password&name=%EC%9D%B4%EB%8F%99%EA%B7%9C&email=brainbackdoor%40gmail.com"
        );

        final User user = new User(
                "cu",
                "password",
                "이동규",
                "brainbackdoor@gmail.com"
        );

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket, new HandlerMapper());

        // when
        handler.run();

        // then
        final User savedUser = DataBase.findUserById("cu");
        assertThat(user.getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(user.getPassword()).isEqualTo(savedUser.getPassword());
        assertThat(user.getName()).isEqualTo(savedUser.getName());
        assertThat(user.getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    void createUserRedirect() {
        // given
        final String httpRequest = String.join("\r\n",
                "POST /user/create HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: 92 ",
                "Content-Type: application/x-www-form-urlencoded ",
                "Accept: */* ",
                "",
                "userId=cu&password=password&name=%EC%9D%B4%EB%8F%99%EA%B7%9C&email=brainbackdoor%40gmail.com"
        );

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket, new HandlerMapper());

        // when
        handler.run();

        // then
        var expected = "HTTP/1.1 302 Found \r\n" +
                "Location: /index.html \r\n" +
                "\r\n";

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    void loginFailedRedirect() {
        // given
        DataBase.addUser(new User("cu", "password", "name", "email"));
        final String httpRequest = String.join("\r\n",
                "POST /user/login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: 32 ",
                "Content-Type: application/x-www-form-urlencoded ",
                "Accept: */* ",
                "",
                "userId=hyeonmo&password=password"
        );

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket, new HandlerMapper());

        // when
        handler.run();

        // then
        var expected = "HTTP/1.1 302 Found \r\n" +
                "Location: /user/login_failed.html \r\n" +
                "\r\n";

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    void loginRedirect() {
        // given
        DataBase.addUser(new User("cu", "password", "name", "email"));
        final String httpRequest = String.join("\r\n",
                "POST /user/login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: 27 ",
                "Content-Type: application/x-www-form-urlencoded ",
                "Accept: */* ",
                "",
                "userId=cu&password=password"
        );

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket, new HandlerMapper());

        // when
        handler.run();

        // then
        assertThat(socket.output()).contains("HTTP/1.1 302 Found \r\n",
                "Set-Cookie: JSESSIONID=",
                "Location: /index.html \r\n",
                "\r\n");
    }
}
