package org.cejug.event.web.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.cejug.business.UserAccountBsn;
import org.cejug.entity.UserAccount;
import org.cejug.event.business.AttendeeBsn;
import org.cejug.event.business.EventBsn;
import org.cejug.event.entity.Attendee;
import org.cejug.event.entity.Event;
import org.cejug.partnership.business.PartnerBsn;
import org.cejug.partnership.entity.Partner;
import org.cejug.web.util.WebTextUtils;

@ManagedBean
@RequestScoped
public class EventBean {

    @EJB
    private EventBsn eventBsn;
    
    @EJB
    private AttendeeBsn attendeeBsn;
    
    @EJB
    private PartnerBsn partnerBsn;
    
    @EJB
    private UserAccountBsn userAccountBsn;

    @ManagedProperty(value="#{param.id}")
    private String id;

    private Event event;
    private Attendee attendee;
    private List<Event> events;
    private List<Event> commingEvents;
    private List<Partner> venues;
    
    private String selectedVenue;

    public EventBean() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

	public Attendee getAttendee() {
		return attendee;
	}

	public void setAttendee(Attendee attendee) {
		this.attendee = attendee;
	}

	public String getSelectedVenue() {
		return selectedVenue;
	}

	public void setSelectedVenue(String selectedVenue) {
		this.selectedVenue = selectedVenue;
	}

	public Boolean getIsAttending() {
		if(attendee != null)
			return true;
		return false;
	}

	public List<Event> getEvents() {
    	if(events == null)
    		events = eventBsn.findEvents();
        return events;
    }
	
	public List<Event> getCommingEvents() {
    	if(commingEvents == null)
    		commingEvents = eventBsn.findCommingEvents();
        return commingEvents;
    }
    
    public List<Partner> getVenues() {
    	if(venues == null)
    		venues = partnerBsn.findPartners();
        return venues;
    }
    
	public String getFormattedEventDescription() {
    	return WebTextUtils.convertLineBreakToHTMLParagraph(event.getDescription());
    }
    
    public String getFormattedEventDescription(String description) {
    	return WebTextUtils.convertLineBreakToHTMLParagraph(description);
    }
    
    public String getFormattedStartDate() {
    	return WebTextUtils.getFormattedDate(event.getStartDate());
    }
    
    public String getFormattedStartDate(Date startDate) {
    	return WebTextUtils.getFormattedDate(startDate);
    }
    
    public String getFormattedEndDate() {
    	return WebTextUtils.getFormattedDate(event.getEndDate());
    }
    
    public String getFormattedStartTime() {
    	return WebTextUtils.getFormattedTime(event.getStartTime());
    }
    
    public String getFormattedStartTime(Date startTime) {
    	return WebTextUtils.getFormattedTime(startTime);
    }
    
    public String getFormattedEndTime() {
    	return WebTextUtils.getFormattedTime(event.getEndTime());
    }
    
    public String getFormattedEndTime(Date endTime) {
    	return WebTextUtils.getFormattedTime(endTime);
    }
    
    public String getFormattedRegistrationDate() {
    	if(this.attendee == null)
    		return "";
    	return WebTextUtils.getFormattedDate(this.attendee.getRegistrationDate());
    }
    
    @PostConstruct
    public void load() {
        if(id != null && !id.isEmpty()) {
            this.event = eventBsn.findEvent(id);
            this.selectedVenue = this.event.getVenue().getId();
            
            HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
    		String username = request.getRemoteUser();
    		UserAccount person = userAccountBsn.findUserAccountByUsername(username);
    		this.attendee = attendeeBsn.findAttendee(this.event, person);
        }
        else {
            this.event = new Event();
        }
    }

    public String confirmAttendance() {
    	HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String username = request.getRemoteUser();
        UserAccount person = userAccountBsn.findUserAccountByUsername(username);
        
        this.event = eventBsn.findEvent(event.getId());
        
        Attendee attendee = new Attendee();
        attendee.setEvent(this.event);
        attendee.setAttendee(person);
        attendee.setRegistrationDate(Calendar.getInstance().getTime());
        attendeeBsn.save(attendee);
        
    	return "events?faces-redirect=true";
    }
    
    public String cancelAttendance() {
    	HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String username = request.getRemoteUser();
        UserAccount person = userAccountBsn.findUserAccountByUsername(username);
        
        this.event = eventBsn.findEvent(event.getId());
        
        Attendee attendee = attendeeBsn.findAttendee(event, person);
        attendeeBsn.remove(attendee.getId());
        
    	return "events?faces-redirect=true";
    }
        
    public String save() {
    	Partner venue = partnerBsn.findPartner(selectedVenue);
    	this.event.setVenue(venue);
    	eventBsn.save(this.event);
        return "events?faces-redirect=true";
    }

    public String remove() {
        eventBsn.remove(this.event.getId());
        return "events?faces-redirect=true";
    }
}