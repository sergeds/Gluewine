import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;



public class ModifyAnswer extends GluewineServlet {
	
	@Override
	public String getContextPath() {
		return "ModifyAnswer";
	}
	
	@Glue
    private HibernateSessionProvider provider;
	
	@Transactional
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
		
		String newFirstname = req.getParameter("firstname");
        String newLastname = req.getParameter("lastname");
        String newEmail = req.getParameter("email");
        String newPhone = req.getParameter("phone");
}
}
