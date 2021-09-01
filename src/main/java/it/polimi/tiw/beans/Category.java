package it.polimi.tiw.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private int position;

    private Boolean isTop = false;
    private List<Category> subClasses = new ArrayList<>();


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Boolean getTop() {
        return isTop;
    }

    public void setTop(Boolean top) {
        isTop = top;
    }

    public List<Category> getSubClasses() {
        return subClasses;
    }

    public void addSubClass(Category category) {
        subClasses.add(category);
    }

    public void removeSubClass(Category category) {
        subClasses.remove(category);
    }


    /*
    public String toString() {
        StringBuffer aBuffer = new StringBuffer("Category");
        aBuffer.append(" id: ");
        aBuffer.append(id);
        aBuffer.append(" name: ");
        aBuffer.append(name);
        aBuffer.append(" subparts: ");
        return aBuffer.toString();
    }

     */



}
