package it.polimi.tiw.controllers;

import com.google.gson.GsonBuilder;
import it.polimi.tiw.beans.Category;
import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.utils.ConnectionHandler;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import com.google.gson.Gson;


@WebServlet("/GetCategoryListData")
public class GetCategoryListData extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }



    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        List<Category> topCategories = null;

        CategoryDAO categoryDAO = new CategoryDAO(connection);


        try {
            topCategories = categoryDAO.findTopsAndSubtrees();

        } catch(Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Impossible to retrieve categories");
            return;
        }

        // Redirect to the Home page and add categories to the parameters

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(topCategories);
        response.setContentType("application/json");
        response.setCharacterEncoding("ISO-8859-1");
        response.getWriter().write(json);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
