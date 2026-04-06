package com.oj.dto;

public class AdminHourlyActivityResponse {
    private final String date;
    private final int hourOfDay;
    private final long total;
    private final long accepted;
    private final double acRate;
    private final long activeUsers;

    public AdminHourlyActivityResponse(String date, int hourOfDay, long total, long accepted, double acRate, long activeUsers) {
        this.date = date;
        this.hourOfDay = hourOfDay;
        this.total = total;
        this.accepted = accepted;
        this.acRate = acRate;
        this.activeUsers = activeUsers;
    }

    public String getDate() {
        return date;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public long getTotal() {
        return total;
    }

    public long getAccepted() {
        return accepted;
    }

    public double getAcRate() {
        return acRate;
    }

    public long getActiveUsers() {
        return activeUsers;
    }
}
