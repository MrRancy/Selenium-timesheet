package com.mindtree.timesheet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Purpose : Time Sheet Automation 
 * Author : Teja Surisetty 
 * Date : 17 June 2019
 **/

public class App {

	// URL of the Attendance
	final static String FINAL_URL = "https://webapps.mindtree.com/MAS/forms/AttendanceReport.aspx";
	static Scanner inputScanner = new Scanner(System.in);

	public static void main(String[] args) throws InterruptedException, IOException, AddressException, MessagingException {
		

		// Start
		Intro.start();
		System.out.println("");

		// Default Hours String
		String totalHours = "//*[@id=\"VisibleReportContentctl00_ContentRequestor_ReportViewer1_ctl09\"]"
				+ "/div/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr/"
				+ "td/table/tbody/tr[3]/td[10]/div";

		// Start and End Dates (Change as per required)
		System.out.println("Enter Start Date in M/D/YYYY Format, ex : 7/1/2019");
		String startDate = inputScanner.next();
		System.out.println("Enter End Date in M/D/YYYY Format, ex : 7/31/2019");
		String endDate = inputScanner.next();

		// Finding difference between Start and End
		int differenceDays = getDifferenceInDays(startDate, endDate);
		System.out.println("Calculating Total Time for " + differenceDays + "days.");
		
		// Setting Crome Driver and System property
		WebDriverManager.chromedriver().setup();
//		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir")+"\\Driver\\chromedriver.exe");

		ArrayList<LocalTime> time = new ArrayList<LocalTime>();
		double output = 0;
		final Calendar c = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
		c.clear();

		// Casting a new Webdriver
		WebDriver chrome = new ChromeDriver();

		// Initial URL is opened
		chrome.get(FINAL_URL);
		chrome.manage().window().maximize();

		// Check if Login needed
		if (!checkForLogin(chrome.getCurrentUrl(), chrome))
			return;

		// If logged sucessfully, Continues to execute
		Thread.sleep(3000);
		
				
		System.out.println("Successfully Opened URL : " + chrome.getTitle());

		// Adding the Date Ranges to get the Time sheet
		chrome.findElement(By.xpath("//*[@id=\"ctl00_ContentRequestor_txtFromDate\"]")).clear();
		chrome.findElement(By.xpath("//*[@id=\"ctl00_ContentRequestor_txtFromDate\"]")).sendKeys(startDate);
		Thread.sleep(1000);
		chrome.findElement(By.xpath("//*[@id=\"ctl00_ContentRequestor_txtToDate\"]")).clear();
		chrome.findElement(By.xpath("//*[@id=\"ctl00_ContentRequestor_txtToDate\"]")).sendKeys(endDate);
		Thread.sleep(1000);
		chrome.findElement(By.xpath("//*[@id=\"ctl00_ContentRequestor_btnSearch\"]")).click();
		Thread.sleep(3000);
		System.out.println("Successfully Fetched Timesheets from " + startDate + " to " + endDate);

		// Fetching all the Times Recorded
		for (int i = 3; i < (differenceDays + 3); i++) {
			String data = totalHours.substring(0, 165) + i + totalHours.substring(166, totalHours.length());

			if (chrome.findElements(By.xpath(data)).size() != 0) {
				time.add(LocalTime.parse(chrome.findElement(By.xpath(data)).getText()));
			} else {
				time.add(LocalTime.parse("00:00"));
			}
		}

		// Updating details in TimeSheet Respectively
		updateTimeSheet(time, chrome);

		// Adding Data for total time purpose
		for (int i = 0; i < time.size(); i++) {
			output += (time.get(i).getHour() * 60);
			output += time.get(i).getMinute();
		}
		
		int total_hours = Math.toIntExact((long) (output/60));
		double total_minutes = (output/60 - total_hours);
		total_minutes = total_minutes * 60;
		
		Intro.end();
		System.out.println("");
		
		// Printing Data in all the respective Time Formats (HH,MM,SS)
		System.out.println("Total Time in Hours : " + total_hours + " Hours & " + Math.round(total_minutes) + " Minutes");
		System.out.println("Total Time in Minutes : " + output);
		System.out.println("Total Time in Seconds : " + output * 60);
		
		Mail.sendEmail();
		
		inputScanner.close();

	}

	private static int getDifferenceInDays(String startDate, String endDate) {

		DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive()
				.append(DateTimeFormatter.ofPattern("M/d/yyyy")).toFormatter();

		MonthDay monthDay = MonthDay.parse(startDate, formatter);
		LocalDate start = monthDay.atYear(2019); 
		System.out.println("start : "+start);
		monthDay = MonthDay.parse(endDate, formatter);
		LocalDate end = monthDay.atYear(2019);
		System.out.println("end : "+end);

		return (int) (ChronoUnit.DAYS.between(start, end) + 1); 
	}

	private static boolean checkForLogin(String currentUrl, WebDriver chrome) throws InterruptedException {
		if (!currentUrl.equals(FINAL_URL)) {
			System.out.println("Please enter your USERNAME: ");
			String username = inputScanner.next();
	        String enteredPassword = inputScanner.next();
			chrome.findElement(By.xpath("//*[@class=\"credentials_input_text\"]")).sendKeys(username);
			chrome.findElement(By.xpath("//*[@class=\"credentials_input_password\"]")).sendKeys(enteredPassword);
			chrome.findElement(By.xpath("//*[@class=\"credentials_input_submit\"]")).click();
			Thread.sleep(500);
		}
		if (chrome.getCurrentUrl().equals(FINAL_URL))
			return true;
		else
			return false;
	}

	private static void updateTimeSheet(ArrayList<LocalTime> time, WebDriver chrome) throws InterruptedException {
		Map<String, LocalTime> dateTime = new LinkedHashMap<String, LocalTime>();
		dateTime = getDateTimeMap(time);
		chrome.close();

//		chrome.get("https://essapps.mindtree.com/sites/TimeSheet/_layouts/15/TimeSheetPh2/Timesheet.aspx");
//		Thread.sleep(2000);
//		String presentDate = chrome.findElement(By.xpath("//*[@id=\"ctl00_PlaceHolderMain_txtStartDate\"]")).getAttribute("value");
//		System.out.println(presentDate);
//		
//		LocalDate present = LocalDate.parse(presentDate); // Check This Statement Start working from here
//		
//		presentDate = present.format(DateTimeFormatter.ofPattern("M/dd/YYYY"));
//		System.out.println(presentDate);

		// *[@id="ctl00_PlaceHolderMain_imgPrevWeek"]
		// *[@id="ctl00_PlaceHolderMain_imgNextWeek"]

	}

	// Generating <Key,Value> pairs
	private static Map<String, LocalTime> getDateTimeMap(ArrayList<LocalTime> time) {
		Map<String, LocalTime> dateTime = new LinkedHashMap<String, LocalTime>();

		for (int i = 0; i < time.size(); i++) {
			LocalDate startingDate = LocalDate.of(2019, Month.AUGUST, i + 1);
			if ((i + 1) < 10)
				dateTime.put(startingDate.format(DateTimeFormatter.ofPattern("M/d/YYYY")), time.get(i));
			else
				dateTime.put(startingDate.format(DateTimeFormatter.ofPattern("M/dd/YYYY")), time.get(i));
		}

		for (Map.Entry<String, LocalTime> entry : dateTime.entrySet())
			System.out.println(entry.getKey() + " - " + entry.getValue());

		return dateTime;
	}
}