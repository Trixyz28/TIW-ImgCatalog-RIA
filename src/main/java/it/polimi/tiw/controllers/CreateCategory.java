package it.polimi.tiw.controllers;

import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.text.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/CreateCategory")
public class CreateCategory extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection = null;
    /*
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
     */

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());

        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter id with format number is required");
            return;
        }

        CategoryDAO categoryDAO = new CategoryDAO(connection);

        try {
            categoryDAO.createCategory(name,fid);

        } catch(Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error in creating the category");
            return;
        }

        String contextPath = getServletContext().getContextPath();
        String path = contextPath + "/GoToHomePage";
        response.sendRedirect(path);

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
