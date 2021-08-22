package it.polimi.tiw.controllers;

import com.google.gson.Gson;
import it.polimi.tiw.beans.Category;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@WebServlet("/MoveCategory")
public class MoveCategory extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private Map<Integer, Integer> modifiedData = new HashMap<>();
    private Boolean done = false;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());

    }


    /*
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

     */



    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        StringBuffer buffer = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                buffer.append(line);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Error in parsing input");
            return;
        }

        String result = buffer.toString();

        // System.out.println(result);

        Gson gson = new Gson();
        int[][] arr;

        try {
            arr = gson.fromJson(result,int[][].class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong format");
            return;
        }


        CategoryDAO categoryDAO = new CategoryDAO(connection);

        try {
            categoryDAO.handleMove(arr);

        } catch(Exception e) {

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in moving the category");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print("Operation completed successfully");

    }

}
