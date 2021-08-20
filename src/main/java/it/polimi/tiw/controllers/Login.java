package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


@WebServlet("/Login")
@MultipartConfig
public class Login extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;


    public void init() throws ServletException{
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    /*
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }*/

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // obtain and escape params
        String username = null;
        String password = null;


        username = StringEscapeUtils.escapeJava(request.getParameter("username"));
        password = StringEscapeUtils.escapeJava(request.getParameter("password"));

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Credentials must be not null");
            return;
        }


        // query db to authenticate for user
        UserDAO userDao = new UserDAO(connection);
        User user = null;
        try {
            user = userDao.checkCredentials(username, password);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Internal server error, retry later");
            return;
        }

        // If the user exists, add info to the session and go to home page, otherwise
        // show login page with error message

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Incorrect credentials");

        } else {
            // Save user in the session attributes
            request.getSession().setAttribute("user", user);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(user.getName() + ' ' + user.getSurname());
        }
    }
}
