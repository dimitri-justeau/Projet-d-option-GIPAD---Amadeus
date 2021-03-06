package io.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import context.userConstraints.cg.CG;
import context.userConstraints.cg.CG00;
import context.userConstraints.cg.CG01;
import context.userConstraints.cve.CVE;
import context.userConstraints.cvf.CVF;
import context.userConstraints.cvo.CVO;

/**
 * impl�mentation de l'interface RequestLoader, 
 * qui permet de charger des fichiers de requ�tes
 * @author Pierre Chouin
 *
 */
public class RequestLoaderImp implements RequestLoader {


    /**
     * Contraintes sur la ville d'origine
     */
    private CVO cvo;

    /**
     * COntraintes sur la ville d'arriv�e
     */
    private CVF cvf;

    /**
     * liste de contraintes sur les villes-�tapes
     */
    private List<CVE> cves;

    /**
     * contraintes g�n�rales
     */
    private List<CG> cgs;

    @Override
    public boolean loadRequest(final String dir) {
        return loadRequest(new File(dir));
    }

    @Override
    public boolean loadRequest(final File file) {
        cves = new ArrayList<CVE> ();
        cgs = new ArrayList<CG> ();
        Scanner sc;
        try {
            sc = new Scanner(file);
            String s="";
            while(sc.hasNextLine()){

                s=sc.nextLine();
                if(s.length()>0&&s.substring(0, ReaderConstants.TAILLE_CV).equals(ReaderConstants.CVO)){
                    String CVO0 = getStringPropre(s, ReaderConstants.TAILLE_CV_COMPLET);
                    s=sc.nextLine();
                    String[] CVO1 = getStringPropre(s, ReaderConstants.TAILLE_CV_COMPLET)
                            .split(ReaderConstants.SEPARATEUR);
                    s=sc.nextLine();
                    String[] CVO2 = getStringPropre(s, ReaderConstants.TAILLE_CV_COMPLET)
                            .split(ReaderConstants.SEPARATEUR);
                    cvo= new CVO(CVO0, CVO1, CVO2);
                }

                if(s.length()>0&&s.substring(0, ReaderConstants.TAILLE_CV).equals(ReaderConstants.CVE)){
                    String nomCVE = getStringPropre(s, 0);
                    s=sc.nextLine();
                    String CVE0 = getStringPropre(s, ReaderConstants.TAILLE_CV_COMPLET);
                    s=sc.nextLine();
                    String CVE1s = getStringPropre(s, ReaderConstants.TAILLE_CV_COMPLET);
                    boolean CVE1=true;
                    if(Integer.parseInt(CVE1s)==0){ CVE1=false; }
                    s=sc.nextLine();
                    String[] CVE2 = getStringPropre(s, ReaderConstants.TAILLE_CV_COMPLET)
                            .split(ReaderConstants.SEPARATEUR);
                    s=sc.nextLine();
                    String[] CVE3s = getStringPropre(s, ReaderConstants.TAILLE_CV_COMPLET)
                            .split(ReaderConstants.SEPARATEUR);
                    int [] CVE3 = {Integer.parseInt(CVE3s[0]), 
                            Integer.parseInt(CVE3s[1])};
                    s=sc.nextLine();
                    String[] CVE4 = getStringPropre(s, ReaderConstants.TAILLE_CV_COMPLET)
                            .split(ReaderConstants.SEPARATEUR);
                    s=sc.nextLine();
                    int CVE5 = Integer.parseInt(getStringPropre(s,
                            ReaderConstants.TAILLE_CV_COMPLET));
                    CVE cve= new CVE(nomCVE, CVE0, CVE1, CVE2, CVE3, CVE4, CVE5);
                    cves.add(cve);          
                }

                if (s.length()>0&&s.substring(0, ReaderConstants.TAILLE_CV).equals(ReaderConstants.CVF)){
                    String CVF0 = getStringPropre(s, ReaderConstants.TAILLE_CV_COMPLET);
                    s=sc.nextLine();
                    String[] CVF1 = getStringPropre(s, ReaderConstants.TAILLE_CV_COMPLET)
                            .split(ReaderConstants.SEPARATEUR);
                    s=sc.nextLine();
                    String[] CVF2 = getStringPropre(s, ReaderConstants.TAILLE_CV_COMPLET)
                            .split(ReaderConstants.SEPARATEUR);
                    cvf= new CVF(CVF0, CVF1, CVF2);
                }

                if (s.length()>0&&s.substring(0, ReaderConstants.TAILLE_CG).equals(ReaderConstants.CG00)){
                    String CG0s = getStringPropre(s, ReaderConstants.TAILLE_CG_COMPLET);
                    String[] CG0 = CG0s.split(",");
                    if(CG0.length==2){
                        int CG0Min= Integer.parseInt(CG0[0].trim());
                        int CG0Max= Integer.parseInt(CG0[1].trim());
                        cgs.add(new CG00(CG0Min, CG0Max));
                    }
                }

                if (s.length()>0&&s.substring(0, ReaderConstants.TAILLE_CG).equals(ReaderConstants.CG01)){
                    String CG1s = getStringPropre(s, ReaderConstants.TAILLE_CG_COMPLET);
                    String[] CG1 = CG1s.split("<");
                    if(CG1.length==2){
                        String CVEInf = CG1[0];
                        String CVESup = CG1[1];
                        cgs.add(new CG01(getCVEIndex(CVEInf), getCVEIndex(CVESup)));//TODO index ou index-1?
                    }
                }

            }
            sc.close();
            return (cvo != null && cvf != null)? true : false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    /**
     * en partant du nom, obtenir la CVE, null si elle n'existe pas
     * @param nomCVE nom de la CVE
     * @return la CVE
     */
    public int getCVEIndex(final String nomCVE){
        for(int i=0; i<cves.size(); i++){
            if(cves.get(i).getNom().equals(nomCVE)){
                return i;
            }
        }
        return -1;
    }




    @Override
    public CVO getCVO() {
        return cvo;
    }

    @Override
    public CVF getCVF() {
        return cvf;
    }

    @Override
    public List<CVE> getCVEs() {
        return cves;
    }

    @Override
    public List<CG> getCGs() {
        return cgs;
    }

    /**
     * retourne la String sans commentaires, sans la contrainte avant, 
     * et sans espaces, ni avant, ni apr�s
     * @param s la String que l'on veut modifier
     * @param nbCarInutiles le nombre de caract�res que l'on supprime 
     * au d�but de la cha�ne de caract�res
     * @return la String modifi�e
     */
    private String getStringPropre(final String s, final int nbCarInutiles){
        return s.substring(nbCarInutiles).split(ReaderConstants.COMMENTAIRE)[0].trim();
    }

}
