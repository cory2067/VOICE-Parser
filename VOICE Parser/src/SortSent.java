
public class SortSent implements Comparable<SortSent>
{
	public int sindex, cindex;
	public double weight;
	
	public SortSent(int sind, int cind, double w)
	{
		sindex = sind;
		cindex = cind;
		weight = w;
	}
	
	public int compareTo(SortSent b)
	{
		if(this.weight > b.weight)
			return 1;
		if(b.weight < this.weight)
			return -1;
		return 0;
	}
}
