package me.jimmyhuang.popularmovies.model;

public class Review {
    private String author;
    private String review;

    public Review(String author, String review) {
        this.author = author;
        this.review = review;
    }

    public void setAuthor(String author) { this.author = author; }
    public String getAuthor() { return author; }

    public void setReview(String review) { this.review = review; }
    public String getReview() { return review; }
}
