package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private Connection connection;

    public CategoryDAO(Connection connection) {
        this.connection = connection;
    }


    public List<Category> findAllCategories() throws SQLException {

        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM category ORDER BY position ASC";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    Category newCat = registerCategory(result);
                    categories.add(newCat);
                }
            }
        }
        return categories;
    }


    public List<Category> findTopsAndSubtrees() throws SQLException {

        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM category WHERE id NOT IN (select child FROM relations) ORDER BY position ASC";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            try (ResultSet result = pstatement.executeQuery();) {
                while (result.next()) {
                    Category newCat = registerCategory(result);
                    newCat.setTop(true);
                    categories.add(newCat);
                }
                for (Category c: categories) {
                    findSubclasses(c);
                }
            }
        }

        return categories;
    }

    public void findSubclasses(Category c) throws SQLException {

        String query = "SELECT C.id, C.name, C.position, C.num_child FROM relations R JOIN category C on C.id = R.child WHERE R.father = ? ORDER BY C.position ASC";

        try (PreparedStatement pstatement = connection.prepareStatement(query);) {

            pstatement.setInt(1, c.getId());

            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    Category newCat = registerCategory(result);
                    findSubclasses(newCat);
                    c.addSubClass(newCat);
                }
            }
        }
    }


    /* Category creation:
    - Trace the father category with fid
    - Check if father's num_child >= 9
    - Calculate the new category's position index
    - Update father's num_child
    - Insert the new category in category table (name, position)
    - Insert the link in relations table
     */
    public void createCategory(String name, int fid) throws Exception {

        int newNumChild = 0;
        int newPosition = 0;

        connection.setAutoCommit(false);

        Category father = findById(fid);

        try {
            if(father==null) {
                throw new SQLException();
            }

            if(father.getNumChild() >= 9) {
                throw new Exception();
            }

            newNumChild = father.getNumChild()+1;
            newPosition = (father.getPosition()*10)+(newNumChild);

            updateNumChild(newNumChild,fid);
            insertNewCategory(name,newPosition);
            addLink(fid,findMaxId());

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }

    }



    /* Move a category:
    - Get cid and newfid
    - Check recursive link #
    - Check if newfather's num_child >= 9 #
    - Trace oldfather with cid from relations table #
    - Take note about the child oldposition #
    - Trace oldfather's other children, update their position index if > oldposition
    - Update oldfather's num_child #
    - Update newfather's num_child #
    - Update child's position
    - Update all its subtrees' position
    - Delete link oldfid - cid from relations table
    - Add link newfid - cid into relations table
     */
    public void moveCategory(int cid, int destid) throws Exception {

        if(cid<=0 || destid<=0) {
            throw new Exception();
        }

        connection.setAutoCommit(false);

        Category chosen = findById(cid);
        Category destination = findById(destid);

        int idOldFather = 0;
        Category oldFather = null;

        if(chosen == null || destination == null) {
            throw new SQLException();
        }

        try {

            if(destination.getNumChild() >= 9) {
                throw new Exception();
            }
            if(cyclicLinkExists(destid,cid)) {
                throw new Exception();
            }

            String query = "SELECT father FROM relations WHERE child = ?";
            try (PreparedStatement pStatement = connection.prepareStatement(query)) {
                pStatement.setInt(1, cid);
                try (ResultSet result = pStatement.executeQuery()) {
                    while (result.next()) {
                        idOldFather = result.getInt(1);
                    }
                }
            }

            int oldChildPosition = chosen.getPosition();

            if(idOldFather != destination.getId()) {
                oldFather = findById(idOldFather);

                int oldNumChild = oldFather.getNumChild();
                oldFather.setNumChild(oldNumChild-1);
                updateNumChild(oldNumChild-1,idOldFather);

                int destNumChild = destination.getNumChild();
                destination.setNumChild(destNumChild+1);
                updateNumChild(destNumChild+1,destid);

                /* Update child's position and its subtrees' ones*/
                int newPosition = (destination.getPosition()*10)+ (destNumChild+1);
                chosen.setPosition(newPosition);
                updatePosition(newPosition,cid);
                findSubclasses(chosen);
                recUpdatePosition(chosen);

                deleteLink(idOldFather, cid);
                addLink(destid,cid);

                /* Update position index of oldfather's other children, if > chosenposition */

                findSubclasses(oldFather);
                if(!oldFather.getSubClasses().isEmpty()) {
                    for(Category c : oldFather.getSubClasses()) {

                        if(c.getPosition() > oldChildPosition) {
                            c.setPosition(c.getPosition()-1);
                            updatePosition(c.getPosition(),c.getId());
                            recUpdatePosition(c);
                        }
                    }
                }

            } else {

                findSubclasses(destination);
                if(!destination.getSubClasses().isEmpty()) {
                    for(Category c : destination.getSubClasses()) {
                        if(c.getId() != cid) {
                            if(c.getPosition() > oldChildPosition) {
                                c.setPosition(c.getPosition()-1);
                                updatePosition(c.getPosition(),c.getId());
                                recUpdatePosition(c);
                            }

                        } else {
                            int newPosition = (destination.getPosition()*10) + destination.getNumChild();
                            c.setPosition(newPosition);
                            updatePosition(c.getPosition(),c.getId());
                            recUpdatePosition(c);
                        }


                    }
                }
            }

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }


    }



    private Category findById(int id) throws SQLException {

        String query = "SELECT * FROM category WHERE id = ?";
        Category category = null;

        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            pStatement.setInt(1, id);
            try (ResultSet result = pStatement.executeQuery()) {
                while (result.next()) {
                    category = registerCategory(result);
                }
            }
        }

        return category;
    }


    private Category registerCategory(ResultSet result) throws SQLException {

        Category cat = new Category();

        cat.setId(result.getInt("id"));
        cat.setName(result.getString("name"));
        cat.setNumChild(result.getInt("num_child"));
        cat.setPosition(result.getInt("position"));

        return cat;
    }


    private void updateNumChild(int numChild,int fid) throws SQLException {
        String query = "UPDATE category SET num_child = ? WHERE id = ?";
        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            pStatement.setInt(1,numChild);
            pStatement.setInt(2,fid);
            pStatement.executeUpdate();
        }
    }

    private void updatePosition(int position, int id) throws SQLException {
        String query = "UPDATE category SET position = ? WHERE id = ?";
        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            pStatement.setInt(1,position);
            pStatement.setInt(2,id);
            pStatement.executeUpdate();
        }
    }


    private void insertNewCategory(String name, int position) throws SQLException {
        String query = "INSERT into category(name, position) VALUES(?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setString(1, name);
            pstatement.setInt(2, position);
            pstatement.executeUpdate();
        }
    }

    private void addLink(int fid, int cid) throws SQLException {
        String query = "INSERT into relations(father, child) VALUES(?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, fid);
            pstatement.setInt(2, cid);
            pstatement.executeUpdate();
        }
    }

    private void deleteLink(int fid, int cid) throws SQLException {
        String query = "DELETE from relations WHERE father = ? AND child = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, fid);
            pstatement.setInt(2, cid);
            pstatement.executeUpdate();
        }
    }

    private int findMaxId() throws SQLException {
        String query = "SELECT MAX(id) FROM category";
        int max = 0;

        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            try (ResultSet result = pStatement.executeQuery()) {
                while (result.next()) {
                    max = result.getInt(1);
                }
            }
        }
        return max;
    }

    private boolean cyclicLinkExists(int p1, int p2) throws SQLException {
        //check if p2 is an ancestor of p1 by using RECURSIVE
        boolean exists = false;

        String query = "with recursive cte (father, child) as (select father, child from relations where child = ? union all select p.father, p.child from relations p inner join cte on p.child = cte.father) select  * from cte where father = ?;";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, p1);
            pstatement.setInt(2, p2);
            // System.out.println(pstatement.toString());
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    exists = true;
                }
            }
        }
        return exists;
    }

    private void recUpdatePosition(Category father) throws SQLException{

        int oldPosition;
        int newPosition;

        if(!father.getSubClasses().isEmpty()) {
            for(Category c : father.getSubClasses()) {

                oldPosition = c.getPosition();
                newPosition = (father.getPosition()*10) + (oldPosition%10);
                c.setPosition(newPosition);

                updatePosition(newPosition,c.getId());
                recUpdatePosition(c);

            }
        }
    }



}
