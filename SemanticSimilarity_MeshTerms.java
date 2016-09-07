    import com.hp.hpl.jena.query.Query;
    import com.hp.hpl.jena.query.QueryExecution;
    import com.hp.hpl.jena.query.QueryExecutionFactory;
    import com.hp.hpl.jena.query.QueryFactory;
    import com.hp.hpl.jena.rdf.model.RDFNode;

    import java.io.*;
    import java.util.ArrayList;

    /**
     * Created by IntelliJ IDEA.
     * User: erajabi
     * Date: 5/24/16
     * Time: 12:52 PM
     * To change this template use File | Settings | File Templates.
     */
    public class SemanticSimilarity_MeshTerms {
        public static String meshEndpoint = "https://id.nlm.nih.gov/mesh/sparql";
        public static String queryString=null;
        public static int count=0;


      public  int mesh_descendant(String termTreeNumber){
            queryString = "PREFIX mesh: <http://id.nlm.nih.gov/mesh/>\n" +
                    "PREFIX mesh2015: <http://id.nlm.nih.gov/mesh/2015/>\n" +
                    "PREFIX mesh2016: <http://id.nlm.nih.gov/mesh/2016/>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX meshv: <http://id.nlm.nih.gov/mesh/vocab#>\n" +
                    "\n" +
                    "SELECT ?t \n" +
                    "\n" +
                    "WHERE {\n" +
                    "    ?s meshv:treeNumber "+ termTreeNumber+".\n" +
                    "    ?o meshv:broaderDescriptor ?s.\n" +
                    "    ?o meshv:treeNumber ?t.FILTER regex(str(?t), \""+termTreeNumber.substring(28,termTreeNumber.length()-1)+"\", \"i\").\n" +
                    "}";
            Query query = QueryFactory.create(queryString);
            // query.serialize(System.out);
            System.out.println("++++++++++++++++++" + count + "+++++++++++++++++++");
            QueryExecution qe = QueryExecutionFactory.sparqlService(meshEndpoint, queryString);
            com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
            if(results.hasNext()==false)
                return 0;
            while (results.hasNext()){
                RDFNode temp=results.next().get("?t");
                System.out.println("== ANSWER =="+temp.toString());
                count++;
                mesh_descendant("<"+temp.toString()+">");
            }
            return count;
        }

        public static String mesh_size (){
            queryString = "PREFIX mesh: <http://id.nlm.nih.gov/mesh/>\n" +
                    "PREFIX mesh2015: <http://id.nlm.nih.gov/mesh/2015/>\n" +
                    "PREFIX mesh2016: <http://id.nlm.nih.gov/mesh/2016/>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX meshv: <http://id.nlm.nih.gov/mesh/vocab#>\n" +
                    "\n" +
                    "SELECT (count(?s) as ?size) \n" +
                    "\n" +
                    "WHERE {\n" +
                    "    ?s meshv:concept ?o.\n" +
                    "}";
            QueryExecution qe = QueryExecutionFactory.sparqlService(meshEndpoint, queryString);
            com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
            return results.next().getLiteral("?size").getString();
        }

        public String find_same_parent (String termTreeNumber1, String termTreeNumber2){
            ArrayList p=new ArrayList<String>();
            ArrayList q=new ArrayList<String>();
            String parent="";
            p.add(termTreeNumber1.substring(0,3));
            q.add(termTreeNumber2.substring(0,3));

            if(termTreeNumber1.length()>3)
                p.add(termTreeNumber1.substring(4,7));
            if(termTreeNumber1.length()>7)
                p.add(termTreeNumber1.substring(8,11));
            if(termTreeNumber1.length()>11)
                p.add(termTreeNumber1.substring(12,15));
            if(termTreeNumber1.length()>15)
                p.add(termTreeNumber1.substring(16,19));
            if(termTreeNumber1.length()>19)
                p.add(termTreeNumber1.substring(20,23));
            if(termTreeNumber1.length()>23)
                p.add(termTreeNumber1.substring(24,27));

            if(termTreeNumber2.length()>3)
                q.add(termTreeNumber2.substring(4,7));
            if(termTreeNumber2.length()>7)
                q.add(termTreeNumber2.substring(8,11));
            if(termTreeNumber2.length()>11)
                q.add(termTreeNumber2.substring(12,15));
            if(termTreeNumber2.length()>15)
                q.add(termTreeNumber2.substring(16,19));
            if(termTreeNumber2.length()>19)
                q.add(termTreeNumber2.substring(20,23));
            if(termTreeNumber2.length()>23)
                q.add(termTreeNumber2.substring(24,27));
            int i=0;
            System.out.println("P="+p.size());
            System.out.println("Q="+q.size());
            while(p.get(i).equals(q.get(i))){
                parent=parent+p.get(i)+".";
                if(i<p.size()-1 && i<q.size()-1)i++;
                else break;
            }
//        System.out.println("parent="+parent.substring(0,parent.length()-1));
            if(parent.equals(""))
                return null;
            return   "<http://id.nlm.nih.gov/mesh/"+parent.substring(0,parent.length()-1)+">";
        }

        public double IC(String temp){
            if(temp==null) return 0;
            int countOfChildren=mesh_descendant(temp);
            return 1-(Math.log(countOfChildren+1)/Math.log(Integer.parseInt(mesh_size())));
        }

        public static void main(String[] args) throws IOException, UnsupportedEncodingException {

            String term1, term2;
            term1="<http://id.nlm.nih.gov/mesh/D02.455.426.559.389.140>";
            term2="<http://id.nlm.nih.gov/mesh/D02.455.426.559.389.261>";
            term1="<http://id.nlm.nih.gov/mesh/D02.455.426.559.389.703>";
            term1="<http://id.nlm.nih.gov/mesh/D02.455.426.559.389.657>";
            // <http://id.nlm.nih.gov/mesh/D02.455.426.559.389.703> and <http://id.nlm.nih.gov/mesh/D02.455.426.559.389.657> ~ 71%
            // <http://id.nlm.nih.gov/mesh/D02.455.426.559.389.703.241> and <http://id.nlm.nih.gov/mesh/D02.455.326.146.100> ~ 59%
            // <http://id.nlm.nih.gov/mesh/D02.455.426.559.389.703.241> and <http://id.nlm.nih.gov/mesh/F01.145.802.975.500> ~ 6%
            term2="<http://id.nlm.nih.gov/mesh/D02.455.426.559.389.657>";

            term1="<http://id.nlm.nih.gov/mesh/C06.130.564.263.500>";
            term2="<http://id.nlm.nih.gov/mesh/E01.370.372.460>";
            System.out.println("******************************");
                    SemanticSimilarity semanticSimilarity=new SemanticSimilarity();
                    System.out.println("term1="+term1);
                    System.out.println("term2="+term2);

                    double IC_term1=0.0, IC_term2=0.0, result=0.0;
                    String parent;
                    parent=semanticSimilarity.find_same_parent(term1.substring(28,term1.length()-1),term2.substring(28,term2.length()-1));
                    IC_term1= semanticSimilarity.IC(term1);
                    IC_term2= semanticSimilarity.IC(term2);

                    //System.out.println(term2.substring(28,term2.length()-1));
                    //System.out.println("Parent="+parent);
                    //System.exit(1);
                    //        System.out.println(countOfChildren);
                    //        System.out.println("Size="+mesh_size());
                    //        double IC=1-(Math.log(countOfChildren+1)/Math.log(Integer.parseInt(mesh_size())));
                    //        System.out.println("IC="+IC);
                    //        sameParent=find_same_parent("D02.455.426.559.389.127.250.311","D02.455.426.559.389");
                    result=(1-(IC_term1+IC_term2-2*semanticSimilarity.IC(parent))/2);
                    System.out.println("Semantic similarity=" + result) ;
                    System.out.println("******************************");

                }

    }

