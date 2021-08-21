package it.polimi.tiw.controllers;

import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


@WebServlet("/CreateCategory")
@MultipartConfig
public class CreateCategory extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;


    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        boolean badRequest = false;

        String name = StringEscapeUtils.escapeJava(request.getParameter("newname"));
        String fidParam = StringEscapeUtils.escapeJava(request.getParameter("fid"));

        int fid = -1;

        if(name == null || fidParam == null) {
            badRequest = true;
        }

        try {
            fid = Integer.parseInt(fidParam);

            if(fid <= 0) {
                badRequest = true;
            }
        } catch (NumberFormatException e) {
            badRequest = true;
        }

        if(badRequest) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong parameter values");
            return;
        }

        CategoryDAO categoryDAO = new CategoryDAO(connection);

        try {
            categoryDAO.createCategory(name,fid);

        } catch(Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in creating the category");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print("Creation completed");

    }


    @Override
    public void destroy() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e){

            }
        }
    }
}
