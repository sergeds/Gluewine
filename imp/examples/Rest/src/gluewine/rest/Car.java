package gluewine.rest;

public class Car {

    private int carId;
    private String carName, make;
    private double price;

    public Car() {  }

    public Car(int carId, String carName, String make, double price) 
    {
        this.carId = carId;
        this.carName = carName;
        this.make = make;
        this.price = price;
    }

    public int getCarId() 
    {
        return carId;
    }

    public void setCarId(int carId) 
    {
        this.carId = carId;
    }

    public String getCarName() 
    {
        return carName;
    }

    public void setCarName(String carName) 
    {
        this.carName = carName;
    }

    public String getMake() 
    {
        return make;
    }

    public void setMake(String make) 
    {
        this.make = make;
    }

    public double getPrice() 
    {
        return price;
    }

    public void setPrice(double price) 
    {
        this.price = price;
    }

    @Override
    public String toString() 
    {
        return String.format("Car Details " +  "[Name=%s, Make=%s, Price=%f]", carName, make, price);
    }
}