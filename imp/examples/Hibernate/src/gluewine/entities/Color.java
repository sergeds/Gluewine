package gluewine.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Color {
	
    @Id
    @GeneratedValue
    private long id = 0;

    private String name = null;

    private int rgb = 0;

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
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the rgb
     */
    public int getRgb()
    {
        return rgb;
    }

    /**
     * @param rgb the rgb to set
     */
    public void setRgb(int rgb)
    {
        this.rgb = rgb;
    }
}