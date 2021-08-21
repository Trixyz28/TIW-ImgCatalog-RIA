package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Category;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet(name = "MoveCategory", value = "/MoveCategory")
public class MoveCategory extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());

    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }



    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // If the user is not logged in (not present in session) redirect to the login
        String loginpath = getServletContext().getContextPath() + "/index.html";
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {
            response.sendRedirect(loginpath);
            return;
        }

        boolean badRequest = false;

        String cidParam = StringEscapeUtils.escapeJava(request.getParameter("categoryid"));
        String destParam = StringEscapeUtils.escapeJava(request.getParameter("destid"));

        int cid = -1;
        int destid = -1;

        if(cidParam == null || destParam == null) {
            badRequest = true;
        }

        try {
            cid = Integer.parseInt(cidParam);
            destid = Integer.parseInt(destParam);

            if(cid <= 0 || destid <= 0) {
                badRequest = true;
            }
        } catch (NumberFormatException e) {
            badRequest = true;
        }

        if(badRequest) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong parameter values");
        }


        CategoryDAO categoryDAO = new CategoryDAO(connection);

        try {
            categoryDAO.moveCategory(cid,destid);

        } catch(Exception e) {

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in moving the category");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print("Operation completed successfully");
    }
}
