import java.util.Comparator;
import java.util.List;

/*
 * Class to modelize a task from a scheduling problem.
 */
class Task implements Comparable<Task> {
    private int id;
    private int est;
    private int lct;
    private int p;
    private int height;
    
    // Attribute used by the timetable edge finding algorithm
    private int pfixed;
    private int pfree;
    private int energyfree;
    private boolean hasfree; 
    
    public int tempEst;
    
    //Attribute used by the Edge-Finder filtering algorithm
    public boolean inLambda;
    
    //Attributes used to map the time attributes of a task to timepoints on the Profile (see Profile.java)
    public Timepoint est_to_timepoint;
    public Timepoint ect_to_timepoint;
    public Timepoint lct_to_timepoint;
    public Timepoint lst_to_timepoint;
    
    /*public TimeTableTimePoint est_to_tttimepoint;
    public TimeTableTimePoint ect_to_tttimepoint;
    public TimeTableTimePoint lct_to_tttimepoint;
    public TimeTableTimePoint lst_to_tttimepoint;*/
    
    public static class ComparatorByEst implements Comparator<Integer> {
        private  Task[] tasks;
        public ComparatorByEst( Task[] list_of_tasks) {
 
            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b) {
         return tasks[a].earliestStartingTime() - tasks[b].earliestStartingTime();
        }
    }
    
    public static class ComparatorByEstEctFree implements Comparator<Integer> {
        private  Task[] tasks;
        public ComparatorByEstEctFree( Task[] list_of_tasks) {
 
            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b){
            if(tasks[a].earliestStartingTime() == tasks[b].earliestStartingTime())
                return tasks[b].earliestStartingTime() + tasks[b].pfree() - tasks[a].earliestStartingTime() - tasks[a].pfree();
            else
                return tasks[a].earliestStartingTime() - tasks[b].earliestStartingTime();
        }
    }
    
    public static class ComparatorByEst_ReverseHeight_ReverseEct implements Comparator<Integer> {
        private  Task[] tasks;
        public ComparatorByEst_ReverseHeight_ReverseEct( Task[] list_of_tasks) {
 
            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b) {
        	if(tasks[a].earliestStartingTime() == tasks[b].earliestStartingTime()){
        		if(tasks[a].height() == tasks[b].height()){
          		  return tasks[b].earliestCompletionTime() - tasks[a].earliestCompletionTime();
          	  	}else{
          	  		return tasks[b].height() - tasks[a].height();
          	  	}
        	}else{
        		return tasks[a].earliestStartingTime() - tasks[b].earliestStartingTime();
        	}
        }
    }
    
    public static class ComparatorByHeight_ReverseEst implements Comparator<Integer> {
          private  Task[] tasks;
          public ComparatorByHeight_ReverseEst( Task[] list_of_tasks) {
   
              this.tasks = list_of_tasks;
          }
          @Override
          public int compare(Integer a, Integer b) {
        	  if(tasks[a].height() == tasks[b].height())
        	  {
        		  return tasks[b].earliestStartingTime() - tasks[a].earliestStartingTime();
        	  }
        	  else
        	  {
        		  return tasks[a].height() - tasks[b].height();
        	  }
          }
      }
    
    public static class ComparatorByHeight implements Comparator<Integer> {
        private  Task[] tasks;

        public ComparatorByHeight( Task[] list_of_tasks) {

            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b) {

        	return tasks[a.intValue()].height() - tasks[b.intValue()].height();

        }
    }

    public static class ComparatorByHeightByLct implements Comparator<Integer> {
        private  Task[] tasks;

        public ComparatorByHeightByLct( Task[] list_of_tasks) {

            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b) {
            if(tasks[a].height() == tasks[b].height())
            {
                return tasks[a].latestCompletionTime() - tasks[b].latestCompletionTime();
            }
            else
            {
                return tasks[a].height() - tasks[b].height();
            }

        }
    }
    
    public static class ReverseComparatorByEst implements Comparator<Integer> {
        private  Task[] tasks;

        public ReverseComparatorByEst( Task[] list_of_tasks) {

            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b) {
        	return tasks[b.intValue()].earliestStartingTime() - tasks[a.intValue()].earliestStartingTime();

        }
    }

    public static class ComparatorByLct implements Comparator<Integer> {    
        private  Task[] tasks;
        public ComparatorByLct( Task[] list_of_tasks) {

            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b) {
           return tasks[a.intValue()].latestCompletionTime() - tasks[b.intValue()].latestCompletionTime();

        }
    }
    
    
    public static class ComparatorByLctByEstByReverseHeightByReverseEct implements Comparator<Integer> {    
        private  Task[] tasks;
        public ComparatorByLctByEstByReverseHeightByReverseEct( Task[] list_of_tasks) {

            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b){
        	if(tasks[a.intValue()].latestCompletionTime() == tasks[b.intValue()].latestCompletionTime())
        	{
        		if(tasks[b.intValue()].earliestStartingTime() == tasks[a.intValue()].earliestStartingTime())
        		{ 
        			if(tasks[a.intValue()].height == tasks[b.intValue()].height)
        				return tasks[b.intValue()].earliestCompletionTime() - tasks[a.intValue()].earliestCompletionTime();
        			else
        				return tasks[b.intValue()].height - tasks[a.intValue()].height;
        		}else{
        			return tasks[a.intValue()].earliestStartingTime() - tasks[b.intValue()].earliestStartingTime();
        		}
        	}else{
        		return tasks[a.intValue()].latestCompletionTime() - tasks[b.intValue()].latestCompletionTime();
        	}

        }
    }
    
    public static class ComparatorByLctByReverseEctByReverseHeightByEst implements Comparator<Integer> {    
        private  Task[] tasks;
        public ComparatorByLctByReverseEctByReverseHeightByEst( Task[] list_of_tasks) {

            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b){
        	if(tasks[a.intValue()].latestCompletionTime() == tasks[b.intValue()].latestCompletionTime())
        	{
        		if(tasks[b.intValue()].earliestCompletionTime() == tasks[a.intValue()].earliestCompletionTime())
        		{ 
        			if(tasks[b.intValue()].height == tasks[a.intValue()].height)
        				return tasks[a.intValue()].earliestStartingTime() - tasks[b.intValue()].earliestStartingTime();
        			else
        				return tasks[b.intValue()].height - tasks[a.intValue()].height;
        		}else{
        			return tasks[b.intValue()].earliestCompletionTime() - tasks[a.intValue()].earliestCompletionTime();
        		}
        	}else{
        		return tasks[a.intValue()].latestCompletionTime() - tasks[b.intValue()].latestCompletionTime();
        	}

        }
    }
    
    public static class ReverseComparatorByLct implements Comparator<Integer> {
        private  Task[] tasks;
        public ReverseComparatorByLct( Task[] list_of_tasks) {

            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b) {
        	return tasks[b.intValue()].latestCompletionTime() - tasks[a.intValue()].latestCompletionTime();

        }
    }

    public static class ComparatorByEct implements Comparator<Integer> {
        private Task[] tasks;
        public ComparatorByEct(Task[] list_of_tasks) {
            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b) {
            return tasks[a].earliestCompletionTime() - tasks[b].earliestCompletionTime();
        }
    }
    
    public static class ComparatorByEctFree implements Comparator<Integer> {
        private Task[] tasks;
        public ComparatorByEctFree(Task[] list_of_tasks) {
            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b) {
            return tasks[a].earliestStartingTime() + tasks[a].pfree() - tasks[b].earliestStartingTime() - tasks[b].pfree();
        }
    }
    

    public static class ComparatorByLst implements Comparator<Integer> {
        private  Task[] tasks;
            public ComparatorByLst( Task[] list_of_tasks) {
  
            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b) {
            return tasks[a].latestStartingTime() - tasks[b].latestStartingTime();

        }
    }
    
    
    public static class ComparatorByP implements Comparator<Integer> {
        private List<Task> tasks;
        public ComparatorByP(List<Task> list_of_tasks) {
            this.tasks = list_of_tasks;
        }
        @Override
        public int compare(Integer a, Integer b) {
            return tasks.get(a.intValue()).processingTime() - tasks.get(b.intValue()).processingTime();
        }
    }
   

    public static class TaskByest implements Comparator<Task> {
        @Override
        public int compare(Task o1, Task o2) {
            return o1.earliestStartingTime() > o2.earliestStartingTime() ? 1 : (o1.earliestStartingTime() < o2.earliestStartingTime() ? -1 : 0);
        }
    }


    public static class TaskBylct implements Comparator<Task> {
        @Override
        public int compare(Task o1, Task o2) {
            return o1.latestCompletionTime() > o2.latestCompletionTime() ? 1 : (o1.latestCompletionTime() < o2.latestCompletionTime() ? -1 : 0);
        }
    }

    public static class TaskBylst implements Comparator<Task> {
        @Override
        public int compare(Task o1, Task o2) {
            return o1.latestStartingTime() > o2.latestStartingTime() ? 1 : (o1.latestStartingTime() < o2.latestStartingTime() ? -1 : 0);
        }
    }

    public static class TaskByect implements Comparator<Task> {
        @Override
        public int compare(Task o1, Task o2) {
            return o1.earliestCompletionTime() > o2.earliestCompletionTime() ? 1 : (o1.earliestCompletionTime() < o2.earliestCompletionTime() ? -1 : 0);
        }
    }
    
    public static class TaskByprocessingtime implements Comparator<Task> {
        @Override
        public int compare(Task o1, Task o2) {
            return o1.processingTime() > o2.processingTime() ? 1 : (o1.processingTime() < o2.processingTime() ? -1 : 0);
        }
    }

    
    public Task(int id, int est, int lct, int p, int h){
        this.id = id;
        this.est = est;
        this.tempEst = est;
        this.lct = lct;
        this.p = p;
        this.height = h;
        this.inLambda = false;
        this.pfixed = Math.max(0, est+2*p-lct);
        this.pfree = p - Math.max(0, est+2*p-lct);
        this.energyfree = (p - Math.max(0, est+2*p-lct))*h;
        this.hasfree = p - Math.max(0, est+2*p-lct) > 0;
    }

   
    
    
    public Task(Task t) {
    	this.id = t.id();
        this.est = t.est;
        this.tempEst = t.tempEst;
        this.lct = t.lct;
        this.p = t.p;
        this.height = t.height;
        this.inLambda = false;
        this.pfixed = Math.max(0, t.est+2*t.p-t.lct);;
        this.pfree = t.p - Math.max(0, t.est+2*t.p-t.lct);
        this.energyfree = (t.p - Math.max(0, t.est+2*t.p-t.lct))*t.height;
        this.hasfree = t.p - Math.max(0, t.est+2*t.p-t.lct) > 0;
    }

     
       
    public int earliestStartingTime() {
        return est;
    }

    public int latestCompletionTime() {
        return lct;
    }

    public int processingTime() {
        return p;
    }

    public int earliestCompletionTime() {
        return est + p;
    }

    public int latestStartingTime() {
        return lct - p;
    }
    
    public int height() {
        return height;
    }
    
    public int energy() {
        return height * p;
    }
    
    public int pfixed(){
        return Math.max(0, est+2*p-lct);
    }
    public int pfree(){
        return p - Math.max(0, est+2*p-lct);
    }
    public int energyfree(){
        return (p - Math.max(0, est+2*p-lct))*height;
    }
    public boolean hasfree(){
        return p - Math.max(0, est+2*p-lct) > 0;
    }
    
    public int envelop(int C){
    	return C * earliestStartingTime() + energy();
    }

    public void updateEstWithTempValue()
    {
    	setEarliestStartingTime(this.tempEst);
    }
    
    public void setTempEarliestStartingTime(int tempEst) {
        if(tempEst > this.tempEst)
        	this.tempEst = tempEst;
    }
    
    public int energyCciLct(int cont, int ci, int C){
    	return cont - (C - ci) * latestCompletionTime();
    }
    public int energyCLct(int cont, int C){
    	return cont - C * latestCompletionTime();
    }
    /* ------------------------------------------------------- */
    
    
    public void setEarliestStartingTime(int est) {
        this.est = est;
    }  
    
    public void setEarliestStartingTimeWithCheck(int est) {
        if(est > this.est)
        	this.est = est;
    }
    
    public void setLatestCompletionTime(int lct) {
        this.lct = lct;
    } 
    
    
    public void setProcessingTime(int p) {
        this.p = p;
    }
    
    
    public boolean pruneEarliestStartingTime(int newest) {
        if (newest > est) {
            est = newest;
            return true;
        }
        return false;
    }

    public boolean pruneLatestCompletionTime(Integer newlct) {
        if ((newlct != null) && (newlct < lct)) {
            lct = newlct;
            return true;
        } else {
            return false;
        }
    }    

    public void fixedPart() {
        if (latestStartingTime() < earliestCompletionTime()) {
            System.out.printf("The fixed part is:(%d", latestStartingTime(), " , %d", earliestCompletionTime(), ")");
            System.out.printf(" , %d", earliestCompletionTime());
            System.out.printf(")\n");
        } else {
            System.out.printf("No fixed part!");
        }
    }

    public int id(){
        return id;
    }

    public boolean isConsistent() {
        return (est + p == lct);
    }
    
    public boolean hasFixedPart() {
        return (lct - p < est + p);
    }
    
    // Returns true if the task has a fixed part that overlaps with the fixed part of another task.
    public boolean haveOverlappingFixedParts(Task another_task) {
        
        return this.hasFixedPart() && another_task.hasFixedPart() && overlap(this.latestStartingTime(),
                this.earliestCompletionTime(),
                another_task.latestStartingTime(),
                another_task.earliestCompletionTime());
    }
    
    private static boolean overlap(int a, int b, int c, int d) {
        assert (a < b && c < d );    
        return ((c < a && a < d) || (c >= a && c < b));

    }


    @Override
    public int compareTo(Task o) {
        return this.est > o.est ? 1 : this.est < o.est ? -1 : 0;
    }

    @Override
    public String toString() {
        return "Task: (est = " + this.est + ", lct = " + this.lct + ", p = " + this.p + ", h = " + this.height +  ", inLambda = " + this.inLambda + ")";
    }
}
