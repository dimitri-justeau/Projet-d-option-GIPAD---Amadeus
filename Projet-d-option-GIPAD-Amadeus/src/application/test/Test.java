package application.test;

import java.util.List;

import model.Flight;

import context.Client;
import context.Context;
import dao.DAO;
import dao.DAOImpl;
import reader.RequestLoader;
import reader.RequestLoaderImp;
import solving.ComplexTripModel;
import solving.ComplexTripSolver;
import solving.SimpleComplexTripModel;
import solving.SimpleComplexTripSolver;

public class Test {

    public static void main(String[] args){
        
        ComplexTripModel model = new SimpleComplexTripModel();
        DAO dao = new DAOImpl();
        
        Context context = new Context(model, dao);
        
        RequestLoader rloader = new RequestLoaderImp();
        Client client = new Client(context, rloader);
        client.loadRequest("res/requests/constraint.txt");
        
        ComplexTripSolver solver = new SimpleComplexTripSolver();
        solver.read(model);
        List<Flight> trip = solver.getFirstTripFound();
        System.out.println("nombre de vols : " + trip.size());
        for(Flight f : trip){
            System.out.println(f);
        }
    }
}
