
public class Timepoint {
	//Attributes used to modelize the linked list
	public Timepoint next;
	public Timepoint previous;
	
	//Attributes of a Timepoint
	public int time;
	public int capacity;
	
	//Attributes used by the Edge-Finder filtering algorithm
	public int increment;
	public int incrementMax;
	public int hMaxTotal;
	public int hreal;
	public int overflow;
	public int cons;
	public int minimumOverflow;
    public int avail;
    public int slackUnder;
    public int slackOver;
    public int overlap;
    public int conflictingTime;
    public int dreal;
    public Timepoint contact;
    public int energy;
    public int l;
	
	public Timepoint(int ptime, int pcapacity)
	{
		next = null;
		previous = null;
		time = ptime;
		capacity = pcapacity;
		increment = 0;
		incrementMax = 0;
		overflow = 0;
		cons = 0;
		minimumOverflow = 0;
		avail = 0;
		hMaxTotal = 0;
		hreal = 0;
        avail = 0;
        slackUnder = 0;
        slackOver = 0;
        overlap = 0;
        conflictingTime = -1;
        dreal = 0;
        contact = null;
        energy = 0;
        l = 1;
	}
	
	public void InsertAfter(Timepoint tp)
	{
		tp.previous = this;
		tp.next = this.next;
		if(next != null)
		{
			next.previous = tp;
		}
		next = tp;
	}
	
	@Override
    public String toString() {
        return "Timepoint : (t = " + this.time + ", next = " + this.next.time + ", c = " + this.capacity + ")";
    }
}
