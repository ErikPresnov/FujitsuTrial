package com.example.SpringBoot;

public class FeeCalculator {

    public void checkParams(String city, String vehicle) {
        if (city != null &&
            !city.equals("tallinn") &&
            !city.equals("tartu") &&
            !city.equals("pärnu"))
            throw new RuntimeException("Unknown location");

        if (vehicle != null &&
            !vehicle.equals("car") &&
            !vehicle.equals("scooter") &&
            !vehicle.equals("bike"))
            throw new RuntimeException("Unknown vehicle type");
    }

    public double calculateBaseFee(String city, String vehicle) {
        checkParams(city, vehicle);
        switch (city) {
            case "tallinn":
                switch (vehicle) {
                    case "car" -> {return 4.0;}
                    case "scooter" -> {return 3.5;}
                    case "bike" -> {return 3.0;}
                }
            case "tartu":
                switch (vehicle) {
                    case "car" -> {return 3.5;}
                    case "scooter" -> {return 3.0;}
                    case "bike" -> {return 2.5;}
                }
            case "pärnu":
                switch (vehicle) {
                    case "car" -> {return 3.0;}
                    case "scooter" -> {return 2.5;}
                    case "bike" -> {return 2.0;}
                }
        }
        return 0;
    }

    public double airTempExtraFee(String vehicle, double airTemp) {
        checkParams(null, vehicle);
        if (vehicle.equals("car")) return 0;
        if (airTemp >= 0) return 0;
        if (-10 > airTemp) return 1;
        return 0.5;
    }

    public double windSpeedExtraFee(String vehicle, double windSpeed) {
        checkParams(null, vehicle);
        if (vehicle.equals("car") || vehicle.equals("scooter")) return 0;
        if (20 < windSpeed)
            throw new RuntimeException("Usage of selected vehicle type is forbidden");
        if (10 < windSpeed && windSpeed < 20) return 0.5;
        return 0;
    }

    public double phenomenonExtraFee(String vehicle, String phenomenon) {
        checkParams(null, vehicle);
        if (vehicle.equals("car")) return 0;
        if (phenomenon.contains("snow") || phenomenon.contains("sleet")) return 1;
        if (phenomenon.contains("rain")) return 0.5;
        if (phenomenon.contains("glaze") ||
            phenomenon.contains("hail") ||
            phenomenon.contains("thunder"))
            throw new RuntimeException("Usage of selected vehicle type is forbidden");
        return 0;
    }
}
