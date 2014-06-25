package gluewine.rest;

import java.util.HashMap;

import org.gluewine.rest.REST;
import org.gluewine.rest.RESTID;

public class Rest1 {
	
    private HashMap<String, Car> cars;

    public Rest1()
    {
        cars = new HashMap<String, Car>();

        cars.put("nano", new Car(1,"Nano","TATA",165000));
        cars.put("tavera", new Car(2, "Tavera", "Chervolet", 865000));
        cars.put("scorpio", new Car(2, "Scorpio", "Mahindra", 870000));
    }

    /*
     * To use this method in the browser, we will need to address the following link:
     *
     * http://localhost:portnumber/REST/test?userid=test
     *
     */
    @REST(path = "/test") //defines the path
    public String helloWorld(@RESTID(id = "userid") String userid)
    {
        return "Hello " + userid + ", welcome to Gluewine";
    }

    /*
     * It is possible to get the return value in an xml-format, the default format used is Json.
     * To change this, we only need to adjust the link we address.
     *
     * When there are no parameters present, we use the following link: 
     * 		localhost:portnumber/REST/getCars?format=xml
     * When there are parameters present we use: 
     * 		localhost:portnumber/REST/getCarName?name=myName&format=xml
     */

    /*
     * To use this method in the browser, we will need to address the following link:
     *
     * http://localhost:portnumber/REST/getCars
     */
    @REST(path = "/getCars")
    public HashMap<String, Car> getAllCars() {
        return cars;
    }


    /*
     * To use this method in the browser, we will need to address the following link:
     *
     * http://localhost:portnumber/REST/getCarName?name=tavera
     */
    @REST(path = "/getCarName")
    public Car getCarDetailsByName(@RESTID( id = "name") String name) throws Exception {

        if(cars.containsKey(name))
            return cars.get(name);
        else
            throw new Exception("Car with name "+name+" not found.");
    }

    /*
     * To use this method in the browser, we will need to address the following link:
     *
     * http://localhost:portnumber/REST/getNumber
     */
    @REST(path = "/getNumber")
    public int getNumber() {
        return 5;
    }

    /*
     * To use this method in the browser, we will need to address the following link:
     *
     * http://localhost:portnumber/REST/getStringArray
     */
    @REST(path = "/getStringArray")
    public String[] getStringArray() {
        String[] testStr = new String [5];
        testStr[0] = "test0";
        testStr[1] = "test1";

        return testStr;
    }
}