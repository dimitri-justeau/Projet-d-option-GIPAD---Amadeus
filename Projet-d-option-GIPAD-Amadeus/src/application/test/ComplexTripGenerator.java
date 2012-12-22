package application.test;

import io.dao.DAO;
import io.dao.csv.DAOImplCSV;
import io.reader.RequestLoader;
import io.reader.RequestLoaderImp;

import java.io.File;

import model.Trip;
import context.Client;
import context.Context;

import solving.ChocoComplexTripSolver;
import solving.IComplexTripSolver;

/**
 * G�n�rateur de voyages complexes.
 * @author Dimitri Justeau
 *
 */
public class ComplexTripGenerator {

    /**
     * Le temps de chargement de la derniere requete 
     * (ie toutes les �tapes pr�c�dent la r�solution).
     */
    private long loadTime;
    
    /**
     * Le temps pass� dans Choco lors de la derniere requete.
     */
    private long chocoTime;
    
    /**
     * Le temps total de traitement de la requete.
     */
    private long totalTime;

    /**
     * Le modele.
     */
    private IComplexTripSolver solver;
    
    /**
     * La requ�te � traiter.
     */
    private File request;
    
    /**
     * Le client de l'application.
     */
    private Client client;
    
    /**
     * True si la requ�te charg�e est valide.
     */
    private boolean requestValid;
    
    /**
     * Constructeur.
     * @param req La requ�te.
     */
    public ComplexTripGenerator (final File req) {
        long t = System.currentTimeMillis();
        
        System.out.println("--------------------------------------------------"
                + "-----------------------------------------------------------"
                + "-----------------------------------------------------------"
                + "---------" + "\n");
        System.out.println("                                                  "
                + "                     COMPLEX TRIP GENERATOR V 0.1 " + "\n");
        System.out.println("--------------------------------------------------"
                + "-----------------------------------------------------------"
                + "-----------------------------------------------------------"
                + "---------" + "\n" + "\n");
        
        request = req;
        solver = new ChocoComplexTripSolver();
        DAO dao = new DAOImplCSV();

        Context context = new Context(solver, dao);

        RequestLoader rloader = new RequestLoaderImp();
        client = new Client(context, rloader);
        requestValid = client.loadRequest(request);
        solver.constraint();
        
        loadTime = System.currentTimeMillis() - t;
        
        System.out.println("\n" + "\n" + "Chargement effectu� en " + loadTime 
                + "ms" + "\n");
    }
    
    /**
     * Essaye de r�soudre la requ�te.
     * @return Le Trip trouv�, ou null si il n'y a pas de solution.
     */
    public Trip tryToSolve() {
        
        if (requestValid) {
            long t = System.currentTimeMillis();
            Trip trip = solver.getFirstTripFound();
    
            chocoTime = System.currentTimeMillis() - t;
            totalTime = loadTime + chocoTime;            
            
            System.out.println("Solution trouv�e en " + chocoTime 
                    + "ms");
            
            return trip;
            
        } else {
            System.out.println("Echec lors du chargement du fichier "
                    + "de requ�te - Format invalide");
            return null;
        }
            
    }
}
