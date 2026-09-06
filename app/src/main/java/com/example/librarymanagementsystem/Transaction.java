package com.example.librarymanagementsystem;

public class Transaction {
    private int requestId;
    private String bookTitle;
    private String issueDate;
    private String dueDate;
    private int fine;

    public Transaction() {}

    public Transaction(int requestId, String bookTitle, String issueDate, String dueDate, int fine) {
        this.requestId = requestId;
        this.bookTitle = bookTitle;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.fine = fine;
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public int getFine() { return fine; }
    public void setFine(int fine) { this.fine = fine; }
}