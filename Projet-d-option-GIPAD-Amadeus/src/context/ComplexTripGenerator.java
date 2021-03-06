package context;

import io.dao.DAO;
import io.dao.csv.DAOImplCSV;
import io.reader.RequestLoader;
import io.reader.RequestLoaderImp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.Trip;

import solving.ChocoComplexTripSolver;
import solving.IComplexTripSolver;


import context.userConstraints.UserConstraint;
import context.userConstraints.cg.CG;
import context.userConstraints.cve.CVE;
import context.userConstraints.cvf.CVF;
import context.userConstraints.cvo.CVO;

/**
 * Repr�sente le g�n�rateur de voyages complexe.
 * Est le point de liaison entre la lecture de requ�te, le
 * moteur de contraintes et la couche d'acc�s au donn�es.
 * @author Dim
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
     * Le solveur.
     */
    private IComplexTripSolver solver;
    
    /**
     * La requ�te � traiter.
     */
    private File request;
    
    /**
     * True si la requ�te charg�e est valide.
     */
    private boolean requestValid;

	/**
	 * Le contexte donnant acc�s � la dao et au moteur de contraintes.
	 */
	private Context context;
	
	/**
	 * Le chargeur de requ�tes.
	 */
	private RequestLoader requestLoader;
	
	/**
	 * La contrainte sur la ville d'origine.
	 */
	private CVO cvo;
	
	/**
	 * La contrainte sur la ville finale.
	 */
	private CVF cvf;
	
	/**
	 * Les contraintes sur les �tapes.
	 */
	private List<CVE> cves;
	
	/**
	 * Les contraintes g�n�rales
	 */
	private List<CG> cgs;
	
	/**
	 * L'ensemble de toutes les contraintes
	 */
	private List<UserConstraint> userConstraints;
	
	/**
	 * Constructeur avec param�tres.
	 * @param ctx Le context.
	 * @param rLoader Le loader de requ�tes.
	 */
	public ComplexTripGenerator(final Context ctx, final RequestLoader rLoader){
		this.context = ctx;
		this.requestLoader = rLoader;
	}
	
	/**
     * Constructeur.
     * @param req La requ�te.
     */
    public ComplexTripGenerator(final File req) {
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
        
        this.userConstraints = new ArrayList<UserConstraint>();
        request = req;
        solver = new ChocoComplexTripSolver();
        DAO dao = new DAOImplCSV();
        this.context = new Context(solver, dao);
        this.requestLoader = new RequestLoaderImp();
        requestValid = loadRequest(request);
        solver.constraint();
        
        loadTime = System.currentTimeMillis() - t;
        
        System.out.println("\n" + "\n" + "Chargement effectu� en " + loadTime 
                + "ms" + "\n");
    }
	
	/**
     * Charge une requ�te dans le client.
     * @param dir Le chemin auquel se trouve le fichier de requ�te.
     */
    public void loadRequest(final String dir){
        loadRequest(new File(dir));
    }
	
	/**
	 * Charge une requ�te dans le client.
	 * @param file Le fichier de requ�te.
	 * @return true si le chargement a r�ussi.
	 */
	public boolean loadRequest(final File file){
	    System.out.println("- CHARGEMENT DE LA REQUETE - ");
	    long t = System.currentTimeMillis();
		// Lecture du fichier de requ�te.
		boolean b = this.requestLoader.loadRequest(file);
		if (b) {
		
    		System.out.print("..........");
    		this.cvo = this.requestLoader.getCVO();
    	    System.out.print("..........");
    		this.cvf = this.requestLoader.getCVF();
    		System.out.print("..........");
    		this.cves = this.requestLoader.getCVEs();
    		System.out.print("..........");
    		this.cgs = this.requestLoader.getCGs();
    		System.out.print("..........");
    		
    		// Hierarchisation des contraintes.
    		this.userConstraints.add(cvo);
    		this.userConstraints.add(cvf);
    		this.userConstraints.addAll(cves);
    		System.out.print(" Ok ! "+"("+(System.currentTimeMillis()-t)+"ms)");
    		
    	    t = System.currentTimeMillis();
    		System.out.println("\n" + "\n"  + "- INITIALISATION DU MODELE -");
    		
    		// Application des contraintes.
    		for(UserConstraint c : this.userConstraints){
    			c.apply(this.context);
    			System.out.print(".....");
    		}
    		System.out.print(" Ok ! "+"("+(System.currentTimeMillis()-t)+"ms)");
    		
    		t = System.currentTimeMillis();
    		System.out.println("\n" + "\n"  
    		        + "- CHARGEMENT DES VOLS DANS LA BASE DE DONNEES -");
    		
    		// Chargement des vols
    		this.loadPossibleFlights();
    		
    	    System.out.print(" Ok ! "+"("+(System.currentTimeMillis()-t)+"ms)");
    		
    		t = System.currentTimeMillis();
    		System.out.println("\n" + "\n"  + "- CONSTRUCTION DU MODELE -");
    		
    		// Initialisation du complex trip model
    		this.context.getComplexTripSolver().build();
    		
    	      // Application des contraintes g�n�rales
            for (CG cg : cgs) {
                cg.apply(context);
            }
    		System.out.print(" Ok ! "+"("+(System.currentTimeMillis()-t)+"ms)");
		}
		return b;
	}
	
	/**
     * Essaye de r�soudre la requ�te.
     * @param obj L'objectif.
     * @return Le Trip trouv�, ou null si il n'y a pas de solution.
     */
    public Trip tryToSolve(final String obj) {
        if (requestValid) {
            long t = System.currentTimeMillis();
            Trip trip = solver.getFirstTripFound(obj);
            chocoTime = System.currentTimeMillis() - t;
            System.out.println("Solution trouv�e en " + chocoTime 
                    + "ms");
            return trip;
        } else {
            System.out.println("Echec lors du chargement du fichier "
                    + "de requ�te - Format invalide");
            return null;
        }
    }
	
	/**
	 * Charge les vols possibles susceptibles de satisfaire les contraintes
	 * du probl�me.
	 */
	private void loadPossibleFlights(){
        int i = 1;
        for(UserConstraint c : this.userConstraints){
            c.loadFlights(context);
            
            System.out.println("Nombre de vols apr�s chargement " + i 
              + "(" + c.getClass().getSimpleName() + ") : " 
              + context.getComplexTripSolver().getPossibleFlights().size());
            
            i++;
        }
	}
}
