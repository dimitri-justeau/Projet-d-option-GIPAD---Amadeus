package context.userConstraints.cvo;

import io.dao.DAO;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import solving.IComplexTripSolver;

import static utils.DateOperations.*;

import model.Airport;
import model.Flight;
import context.Context;
import context.userConstraints.UserConstraint;

/**
 * Repr�sente une contrainte sur la ville d'origine du voyage
 * @author Dim
 *
 */
public class CVO extends UserConstraint {
	
	/**
	 * La ville d'origine.
	 */
	private Airport origin;
	
	/**
	 * L'intervalle de dates pour le d�part.
	 */
	private Date dep1, dep2;
	
	/**
	 * La tranche horaire pendant laquelle peut avoir lieu le d�part.
	 */
	private String h1, h2;
	
	/**
	 * Constructeur avec champs.
	 * @param airport L'aeroport d'origine.
	 * @param depInterval La date min locale du d�part,
	 * sous la forme : YYYY/MM/dd-HH:mm.
	 * @param hourInterval L'heure min du d�part (HH:mm).
	 */
	public CVO(final String airport, final String[] depInterval,
			final String[] hourInterval){
		this.origin = Airport.valueOf(airport);
		String pattern = "yyyy/MM/d-HH:mm";
		try {
			TimeZone tz = this.origin.getTimeZone();
			this.dep1 = getDateFromPattern(pattern, depInterval[0], tz);
			this.dep2 = getDateFromPattern(pattern, depInterval[1], tz);
		} catch (ParseException e) {
			System.out.println("Erreur dans la lecture des dates du"
					+ " fichier de requ�te (CVO)");
			e.printStackTrace();
		}		
        
		this.h1 = hourInterval[0];
		this.h2 = hourInterval[1];
	}

	@Override
	public void apply(final Context context) {
		context.getComplexTripSolver().setStart(origin, dep1, dep2);
	}

	@Override
	public boolean remove(final Flight flight) {
		boolean b = false;
		try {
		   
			b = (flight.getOrigin() == origin
			        && !isBetweenHours(flight.getDeparture(), "HH:mm", h1, h2));

		} catch (ParseException e) {
			System.out.println("Erreur de lecture du format de l'heure dans le"
					+ " fichier de requ�tes (CVO)");
			e.printStackTrace();
		}
		return b;
	}

    @Override
    public void loadFlights(final Context context) {
        IComplexTripSolver cxtm = context.getComplexTripSolver();
        DAO dao = context.getDao();

        // R�cup�ration des dates entre lesquel on va r�cup�rer des vols.
        Date d1 = cxtm.getEarliestDeparture();
        Date d2 = cxtm.getLatestDeparture();
                
        // Ajout des vols
        for (int i = 0; i < cxtm.getStages().size(); i++) {
            List<Airport> stage = new ArrayList<Airport>();
            stage.add(cxtm.getStages().get(i));
            for (Flight f : dao.getFlightsFromAirportToList(origin,
                    stage, d1, d2)) {
                if(!this.remove(f) 
                        && !cxtm.getPossibleFlights().contains(f)
                        && !f.getArrival().after(cxtm.getLatestArrival())
                        && f.getArrival().after(
                                cxtm.getStagesIntervals().get(i)[0])
                        && f.getArrival().before(
                                cxtm.getStagesIntervals().get(i)[1])){
                    cxtm.addPossibleFlight(f);
                }    
            }
        }
    }

}
