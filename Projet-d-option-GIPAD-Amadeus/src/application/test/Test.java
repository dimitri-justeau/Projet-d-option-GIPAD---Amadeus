package application.test;

import model.Trip;

import context.Client;
import context.Context;
import dao.DAO;
import dao.csv.DAOImplCSV;
import reader.RequestLoader;
import reader.RequestLoaderImp;
import solving.ComplexTripModel;
import solving.ComplexTripSolver;
import solving.SimpleComplexTripModel;
import solving.SimpleComplexTripSolver;

public class Test {

    public static void main(String[] args){
        
        for(int i = 0; i < 1; i++) {
            long t = System.currentTimeMillis();
            
            ComplexTripModel model = new SimpleComplexTripModel();
            DAO dao = new DAOImplCSV();
            
            Context context = new Context(model, dao);
            
            RequestLoader rloader = new RequestLoaderImp();
            Client client = new Client(context, rloader);
        
            client.loadRequest("res/requests/test/request 3.txt");
//            client.loadRequest("res/requests/test/request "
//                                        + i + ".txt");
            
            ComplexTripSolver solver = new SimpleComplexTripSolver();
            solver.read(model);
            Trip trip = solver.getFirstTripFound();
            if(trip != null){
                System.out.println(trip + "\n");
            } else {
                System.out.println(" Pas de solution possible " + "\n");
            }
            
            System.out.println("Temps total : " 
                    + (System.currentTimeMillis()-t) + "ms" + "\n");
        }
        
    }
}
