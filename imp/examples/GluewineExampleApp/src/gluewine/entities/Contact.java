package Gluewine.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Contact")
public class Contact
{
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id = 0;
	
	@Column(name = "Firstname")
	private String Firstname;
	 
	@Column(name = "Lastname")
	private String Lastname;
	 
	@Column(name = "PhoneNumber")
	private int PhoneNumber;
	 
	@Column(name = "Email")
	private String Email;
	 
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFirstname() {
		return Firstname;
	}

	public void setFirstname(String firstname) {
		Firstname = firstname;
	}

	public String getLastname() {
		return Lastname;
	}

	public void setLastname(String lastname) {
		Lastname = lastname;
	}

	public int getPhoneNumber() {
		return PhoneNumber;
	}

	public void setPhoneNumber(int phoneNumber) {
		PhoneNumber = phoneNumber;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}	
}