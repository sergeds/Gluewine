package gluewine.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Cars")
public class Car
{
    @Id
    @GeneratedValue
    private long id = 0;

    @Column(name = "Brands")
    private String brand;

    @Column(name = "Models")
    private String model;

    @ManyToOne
    private Color color = null;

    /**
     * @return the id
     */
    public long getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id)
    {
        this.id = id;
    }

    /**
     * @return the brand
     */
    public String getBrand()
    {
        return brand;
    }

    /**
     * @param brand the brand to set
     */
    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    /**
     * @return the model
     */
    public String getModel()
    {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(String model)
    {
        this.model = model;
    }

    /**
     * @return the color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color)
    {
        this.color = color;
    }


}
