package com.driver.controllers;


import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class AirportController {

    public HashMap<String,Airport> airportDb = new HashMap<>();

    public HashMap<Integer, Flight> flightDb = new HashMap<>();

    public HashMap<Integer,List<Integer>> flightToPassengerDb = new HashMap<>();

    public HashMap<Integer,Passenger> passengerDb = new HashMap<>();


    @PostMapping("/add_airport")
    public String addAirport(@RequestBody Airport airport){

        //Simply add airport details to your database
        //Return a String message "SUCCESS"
        airportDb.put(airport.getAirportName(),airport);

        return "SUCCESS";
    }

    @GetMapping("/get-largest-aiport")
    public String getLargestAirportName(){

        //Largest airport is in terms of terminals 3 terminal airport is larger than 2 terminal airport
        //Incase of a tie return the Lexicographically smallest airportName

        String ans = "";
        int terminals = 0;
        for(Airport airport : airportDb.values()){

            if(airport.getNoOfTerminals()>terminals){
                ans = airport.getAirportName();
                terminals = airport.getNoOfTerminals();
            }else if(airport.getNoOfTerminals()==terminals){
                if(airport.getAirportName().compareTo(ans)<0){
                    ans = airport.getAirportName();
                }
            }
        }
        return ans;
    }

    @GetMapping("/get-shortest-time-travel-between-cities")
    public double getShortestDurationOfPossibleBetweenTwoCities(@RequestParam("fromCity") City fromCity, @RequestParam("toCity")City toCity){

        //Find the duration by finding the shortest flight that connects these 2 cities directly
        //If there is no direct flight between 2 cities return -1.

        double distance = 1000000000;

        for(Flight flight:flightDb.values()){
            if((flight.getFromCity().equals(fromCity))&&(flight.getToCity().equals(toCity))){
                distance = Math.min(distance,flight.getDuration());
            }
        }

        if(distance==1000000000){
            return -1;
        }
        return distance;

    }

    @GetMapping("/get-number-of-people-on-airport-on/{date}")
    public int getNumberOfPeopleOn(@PathVariable("date") Date date,@RequestParam("airportName")String airportName){

        //Calculate the total number of people who have flights on that day on a particular airport
        //This includes both the people who have come for a flight and who have landed on an airport after their flight



        Airport airport = airportDb.get(airportName);
        if(Objects.isNull(airport)){
            return 0;
        }
        City city = airport.getCity();
        int count = 0;
        for(Flight flight:flightDb.values()){
            if(date.equals(flight.getFlightDate()))
                if(flight.getToCity().equals(city)||flight.getFromCity().equals(city)){

                    int flightId = flight.getFlightId();
                    count = count + flightToPassengerDb.get(flightId).size();
                }
        }
        return count;
    }

    @GetMapping("/calculate-fare")
    public int calculateFlightFare(@RequestParam("flightId")Integer flightId){

        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be jsut checking price

        int noOfPeopleBooked = flightToPassengerDb.get(flightId).size();
        return noOfPeopleBooked*50 + 3000;

    }


    @PostMapping("/book-a-ticket")
    public String bookATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){

        //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case :
        //return a String "FAILURE"
        //Also if the passenger has already booked a flight then also return "FAILURE".
        //else if you are able to book a ticket then return "SUCCESS"


        if(Objects.nonNull(flightToPassengerDb.get(flightId)) &&(flightToPassengerDb.get(flightId).size()<flightDb.get(flightId).getMaxCapacity())){


            List<Integer> passengers =  flightToPassengerDb.get(flightId);

            if(passengers.contains(passengerId)){
                return "FAILURE";
            }

            passengers.add(passengerId);
            flightToPassengerDb.put(flightId,passengers);
            return "SUCCESS";
        }
        else if(Objects.isNull(flightToPassengerDb.get(flightId))){
            flightToPassengerDb.put(flightId,new ArrayList<>());
            List<Integer> passengers =  flightToPassengerDb.get(flightId);

            if(passengers.contains(passengerId)){
                return "FAILURE";
            }

            passengers.add(passengerId);
            flightToPassengerDb.put(flightId,passengers);
            return "SUCCESS";

        }
        return "FAILURE";
    }

    @PutMapping("/cancel-a-ticket")
    public String cancelATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){

        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message
        // and also cancel the ticket that passenger had booked earlier on the given flightId

        List<Integer> passengers = flightToPassengerDb.get(flightId);
        if(passengers == null){
            return "FAILURE";
        }


        if(passengers.contains(passengerId)){
            passengers.remove(passengerId);
            return "SUCCESS";
        }
        return "FAILURE";
    }


    @GetMapping("/get-count-of-bookings-done-by-a-passenger/{passengerId}")
    public int countOfBookingsDoneByPassengerAllCombined(@PathVariable("passengerId")Integer passengerId){

        //Tell the count of flight bookings done by a passenger: This will tell the total count of flight bookings done by a passenger :
        HashMap<Integer,List<Integer>> passengerToFlightDb = new HashMap<>();
        //We have a list from passenger To flights database:-
        int count = 0;
        for(Map.Entry<Integer,List<Integer>> entry: flightToPassengerDb.entrySet()){

            List<Integer> passengers  = entry.getValue();
            for(Integer passenger : passengers){
                if(passenger==passengerId){
                    count++;
                }
            }
        }
        return count;
    }

    @PostMapping("/add-flight")
    public String addFlight(@RequestBody Flight flight){

        //Return a "SUCCESS" message string after adding a flight.
        flightDb.put(flight.getFlightId(),flight);
        return "SUCCESS";
    }


    @GetMapping("/get-aiportName-from-flight-takeoff/{flightId}")
    public String getAirportNameFromFlightId(@PathVariable("flightId")Integer flightId){

        //We need to get the starting aiport from where the flight will be taking off
        //return null incase the flightId is invalid or you are not able to find the airportName

        if(flightDb.containsKey(flightId)){
            City city = flightDb.get(flightId).getFromCity();
            for(Airport airport:airportDb.values()){
                if(airport.getCity().equals(city)){
                    return airport.getAirportName();
                }
            }
        }
        return null;
    }


    @GetMapping("/calculate-revenue-collected/{flightId}")
    public int calculateRevenueOfAFlight(@PathVariable("flightId")Integer flightId){

        int noOfPeopleBooked = flightToPassengerDb.get(flightId).size();
        int variableFare = (noOfPeopleBooked*(noOfPeopleBooked+1))*25;
        int fixedFare = 3000*noOfPeopleBooked;
        int totalFare = variableFare + fixedFare;

        return totalFare;
    }


    @PostMapping("/add-passenger")
    public String addPassenger(@RequestBody Passenger passenger){

        passengerDb.put(passenger.getPassengerId(),passenger);
        return "SUCCESS";
    }


}
