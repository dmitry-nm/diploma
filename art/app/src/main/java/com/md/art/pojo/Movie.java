package com.md.art.pojo;

/**
 * Created by MD on 10.05.2015.
 */
public class Movie {
    private String name;
    private String hash;
    private int year;
    private String description;
    private String url;
    private Rating[] ratings;

    public Movie()
    {

    }
    public Movie(String name, String hash, int year,String description, String url,Rating[] ratings)
    {
        this.name=name;
        this.hash=hash;
        this.year=year;
        this.description=description;
        this.url=url;
        this.ratings=ratings;
    }

    @Override
    public String toString()
    {
        String r="";
        for (int i=0;i<ratings.length;i++)
            r+=ratings[i].toString()+"\n";
        return name+"("+year+")\n"+r+(description!=null?description:"");
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Rating[] getRatings() {
        return ratings;
    }

    public void setRatings(Rating[] ratings) {
        this.ratings = ratings;
    }
}
