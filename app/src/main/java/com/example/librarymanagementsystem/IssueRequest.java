package com.example.librarymanagementsystem;

public class IssueRequest {
    private int requestId;
    private String userEmail;
    private String userName;
    private String bookId;
    private String bookTitle;
    private String status;

    public IssueRequest() {}

    public IssueRequest(int requestId, String userEmail, String userName, String bookId, String bookTitle, String status) {
        this.requestId = requestId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.status = status;
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}