import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableEvaluator;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;


	/**
	 * <b>Static Variable Order </b> variable selector.
	 * It chooses the leftmost non instantiated variable  (instantiated variables are ignored).
	 */
	public class StaticVarOrder implements VariableSelector<IntVar>, VariableEvaluator<IntVar> {

	    private final IStateInt lastIdx; // index of the last non-instantiated variable

	    /**
	     * <b>Satatic Variable Order</b> variable selector.
	     * @param model reference to the model (does not define the variable scope)
	     */
	    public StaticVarOrder(Model model){
	        lastIdx = model.getEnvironment().makeInt(0);
	    }
	    
	    
	    @Override
	    public IntVar getVariable(IntVar[] variables) {
	        IntVar uninstantiatedVar  = null;
	        //int smallDSize = Integer.MAX_VALUE;
	        // get and update the index of the first uninstantiated variable
	        int idx = lastIdx.get();
	        while(idx < variables.length && variables[idx].isInstantiated()) {
	            idx++;
	        }
	        lastIdx.set(idx);
	        //search for the leftmost uninstantiated variable
	        while(idx < variables.length) {
	            if (!variables[idx].isInstantiated()) {
	             // the variable is candidate since it is not instantiated 
	                uninstantiatedVar = variables[idx];
	            }
	            idx++;
	        }
	        return uninstantiatedVar;
	    }
	    
	    @Override
	    public double evaluate(IntVar variable) {
	        return variable.getDomainSize();
	    }
	}