
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.Smallest;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.ArrayList;

public class runRCPSP {
	
	private static final int VincentQuimper = 0;
    private static final int FetgoKameugne = 1;
    private static final int FetgoKameugneTimeTable = 2;
	
	private static final int Static_Branch = 0;
	private static final int Dynamic_Branch_DOWDS = 1;
	private static final int Dynamic_Branch_MDOMS = 2;

	private int m_numberOfTasks;
    private int m_numberOfResources;

    private int m_solution[];
	private float m_elapsedTime;
    private long m_backtracksNum;
    private long m_visitedNodes;
    private int m_makespan;
    private int m_adjustements;
	
	public runRCPSP(String fileName, int propagator_type, int branch_mode) throws Exception {

        Model model = new Model("RCPSP instance");

		RCPSPInstance data = new RCPSPInstance(fileName);
		this.m_numberOfTasks = data.numberOfTasks;
		this.m_numberOfResources = data.numberOfResources;

        IntVar[] startingTimes = new IntVar[m_numberOfTasks];
        IntVar[] processingTimes = new IntVar[m_numberOfTasks];
        IntVar[] endingTimes = new IntVar[m_numberOfTasks];

        for (int i = 0; i < m_numberOfTasks; i++)
        {
        	startingTimes[i] = model.intVar("s[" + i + "]", 0, data.horizon(), true);
            endingTimes[i] = model.intVar("e[" + i + "]", data.processingTimes[i], data.horizon(), true);
            processingTimes[i] = model.intVar("p[" + i + "]", data.processingTimes[i]);
        }

        //On pose la contrainte indiquant que la premi�re t�che (une t�che dummy) doit commencer au temps 0
        model.arithm(startingTimes[0], "=", 0).post();

        //Makespan
        IntVar makespan = model.intVar("makespan", 0, data.horizon(), true);
        for (int i = 0; i < m_numberOfTasks; i++) {
            model.arithm(endingTimes[i], "<=", makespan).post();
        }
        model.arithm(makespan, "=", endingTimes[m_numberOfTasks-1]).post();

        for(int i = 0; i< m_numberOfTasks; i++)
        {
        	for(int j = i+1; j< m_numberOfTasks; j++)
        	{
        		if(data.precedences[i][j] == 1)
        		{
                    model.arithm(startingTimes[i], "+", processingTimes[i], "<=", startingTimes[j]).post();
        		}
        		else if(data.precedences[i][j] == 0)
        		{
                    model.arithm(startingTimes[j], "+", processingTimes[j], "<=", startingTimes[i]).post();
        		}
        	}
        }

        IntVar[] startingTimes_and_makespan = new IntVar[m_numberOfTasks+1];
        System.arraycopy(startingTimes, 0, startingTimes_and_makespan, 0, m_numberOfTasks);
        startingTimes_and_makespan[m_numberOfTasks] = makespan;

        //On pose les contraintes pour chacune des ressources
        AdjustmentsPropagator[] propagators = new AdjustmentsPropagator[m_numberOfResources];
        for(int i = 0; i< m_numberOfResources; i++)
        {
            IntVar[] heights = new IntVar[m_numberOfTasks];
            for (int j = 0; j < m_numberOfTasks; j++)
            {
                heights[j] = model.intVar("h[" + i + "][" + j +"]", data.heights[i][j]);
            }

            //On filtre les variables qui on un height null
            ArrayList<Integer> indices = new ArrayList<>();
            for(int j=0; j<data.heights[i].length; j++)
            {
                if(data.heights[i][j] > 0)
                {
                    indices.add(j);
                }
            }


        if(indices.size() != 0){
            IntVar[] filtered_startingTimes_makespan = new IntVar[indices.size() + 1];
            IntVar[] filtered_endingTimes = new IntVar[indices.size()];
            Integer[] filtered_heights = new Integer[indices.size()];
            Integer[] filtered_processingTimes = new Integer[indices.size()];
            Task[] filtered_tasks = new Task[indices.size()];
            IntVar[] filtered_heights_var = new IntVar[indices.size()];

            for(int j=0; j<indices.size(); j++)
            {
                int index = indices.get(j);

                //Variables n�cessaires pour notre contrainte
                filtered_startingTimes_makespan[j] = startingTimes[index];
                filtered_endingTimes[j] = endingTimes[index];
                filtered_heights[j] = data.heights[i][index];
                filtered_processingTimes[j] = data.processingTimes[index];

                //Variables n�cessaire pour la contrainte Cumulative de Choco
                filtered_tasks[j] = new Task(startingTimes[index], processingTimes[index], endingTimes[index]);
                filtered_heights_var[j] = heights[index];
            }

            //On ajoute la variable de makespan
            filtered_startingTimes_makespan[indices.size()] = makespan;

            //On cr�e la contrainte et son propagateur

            switch(propagator_type){
            case VincentQuimper:
            	Constraint GingrasQuimperEdgeFinder = new Constraint("Gingras and Quimper Horizontally Ellastic Edge Findere",
                    propagators[i] = new EdgeFinderConstraint(
                            filtered_startingTimes_makespan,
                            filtered_endingTimes,
                            filtered_heights,
                            filtered_processingTimes,
                            data.capacities[i]
                    )
            	);
            	model.post(GingrasQuimperEdgeFinder);
            break;
                case FetgoKameugne:
                    Constraint FetgoKameugneEdgeFinder = new Constraint("Gingras and Quimper Horizontally Ellastic Edge Findere",
                    propagators[i] = new QuadEdgeFinderConstraint(
                            filtered_startingTimes_makespan,
                            filtered_endingTimes,
                             filtered_heights,
                             filtered_processingTimes,
                             data.capacities[i]
                       )
                    );
                 model.post(FetgoKameugneEdgeFinder);
            break;
                case FetgoKameugneTimeTable:
                    Constraint FetgoKameugneTimeTableEdgeFinder = new Constraint("Gingras and Quimper Horizontally Ellastic Edge Findere",
                            propagators[i] = new QuadTimeTableEdgeFinderConstraint(
                                    filtered_startingTimes_makespan,
                                    filtered_endingTimes,
                                    filtered_heights,
                                    filtered_processingTimes,
                                    data.capacities[i]
                            )
                    );
                    model.post(FetgoKameugneTimeTableEdgeFinder);
                    break;
                default:
                model.cumulative(filtered_tasks, filtered_heights_var, model.intVar("capacity", data.capacities[i]), false, Cumulative.Filter.TIME).post();
            }
            //On pose la contrainte
        	model.cumulative(filtered_tasks, filtered_heights_var, model.intVar("capacity", data.capacities[i]), false, Cumulative.Filter.TIME).post();
        }
     }

        model.setObjective(false, makespan);
        Solver solver = model.getSolver();

        
        switch(branch_mode){
            case Static_Branch:
        	    solver.setSearch(Search.intVarSearch(new StaticVarOrder(model), new IntDomainMin(), startingTimes_and_makespan));
        	    break;
            case Dynamic_Branch_DOWDS:
                solver.setSearch(Search.conflictOrderingSearch(Search.domOverWDegSearch(startingTimes_and_makespan)));
        	    break;
            case Dynamic_Branch_MDOMS:
                solver.setSearch(Search.conflictOrderingSearch(Search.minDomLBSearch(startingTimes_and_makespan)));
                break;
        	default:
        	    solver.setSearch(Search.conflictOrderingSearch(Search.intVarSearch(new SmallestVarOrder(model), new IntDomainMin(), startingTimes_and_makespan)));
        }

        solver.setRestartOnSolutions();
        solver.limitTime(1*60*1000);

        Solution best = solver.findOptimalSolution(makespan, false);
        m_solution = new int[m_numberOfTasks];




        if (best == null)
		{
    		//Pas de solution trouvée 
    		m_makespan = -1;
		}
    	else if(solver.isObjectiveOptimal())
    	{
			m_makespan = best.getIntVal(makespan);
			for (int i = 0; i < m_numberOfTasks; i++){
	        		m_solution[i] = best.getIntVal(startingTimes[i]);
	        }
		} 
		else 
		{
			m_makespan = -1;
		}

        //m_makespan = best.getIntVal(makespan);
		m_elapsedTime =  solver.getTimeCount();
        m_backtracksNum = solver.getBackTrackCount();
        m_visitedNodes = solver.getNodeCount(); //Retourne le nombre de noeuds visit�s dans l'arbre.
        m_adjustements = 0;
        for (int i = 0; i < m_numberOfResources; i++) {
            //On filtre les variables qui on un height null
            ArrayList<Integer> indices = new ArrayList<>();
            for(int j=0; j<data.heights[i].length; j++)
            {
                if(data.heights[i][j] > 0)
                {
                    indices.add(j);
                }
            }
            if(indices.size() != 0)
                m_adjustements += propagators[i].getNbAdjustments();
        }
	}
	
	public float howMuchTime() {
        return m_elapsedTime;
    }
    
    public long howManyBacktracks() {
        return m_backtracksNum;
    }
    
    public long howManyVisitedNodes() {
        return m_visitedNodes;
    }

    public int howManyAdjustments() {
	    return m_adjustements;
    }
    
    public int makeSpanSolution() {
        return m_makespan;
    }
    
    public void printResults() {
    	System.out.print(m_makespan + " | " + m_elapsedTime + " | " + m_backtracksNum + " | " + m_adjustements + "\t \t");
    }

    public String getResults() {
        return m_makespan + " | " + m_elapsedTime + " | " + m_backtracksNum + " | " + m_adjustements + "\t \t";
    }

    public void printSolution() {
        for (int i = 0; i < m_numberOfTasks; i++){
            System.out.print("s["+ i + "] = "+ m_solution[i] + " , ");
        }
        System.out.println();
    }
}
