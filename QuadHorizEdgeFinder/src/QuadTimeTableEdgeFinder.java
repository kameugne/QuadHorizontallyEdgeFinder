import java.util.Arrays;
import java.util.Comparator;

public class QuadTimeTableEdgeFinder {
    private Integer C;
    private Integer n;
    private Task[] tasks;
    private Profile tl;
    private Integer[] Prec;

    private Integer[] tasks_indices_lct;
    private Integer[] tasks_indices_est;
    private Integer[] tasks_indices_ect;
    private Integer[] tasks_indices_lst;

    Integer makespan;
    private int[] estPrime;

    public QuadTimeTableEdgeFinder(Task[] tasks, int C)
    {
        this.C = C;
        this.n = tasks.length;
        this.tasks = tasks;
        this.tl = new Profile();
        estPrime = new int[tasks.length];

        Prec = new Integer[tasks.length];
        Arrays.fill(Prec, -1);

        tasks_indices_lct = sortWithJavaLibrary(tasks, new Task.ComparatorByLctByEstByReverseHeightByReverseEct(tasks)); //Increasing LCT
        tasks_indices_est = sortWithJavaLibrary(tasks, new Task.ComparatorByEst(tasks)); //Increasing EST
        tasks_indices_ect = sortWithJavaLibrary(tasks, new Task.ComparatorByEct(tasks)); //Increasing ECT
        tasks_indices_lst = sortWithJavaLibrary(tasks, new Task.ComparatorByLst(tasks)); //Increasing LST

        makespan = Integer.MAX_VALUE;

        InitializeTimeLine();
    }
    // after the detection, the pruning function is called
    public int[] Filter()
    {
        int[] results = new int[tasks.length];
        if(EdgeFinder_Detection())
            results = EdgeFinder_Pruning();
        return results;
    }

    private boolean EdgeFinder_Detection()
    {
        InitializeIncrements(tasks_indices_lct.length - 1);
        makespan = ScheduleTasks(tasks[tasks_indices_lct[tasks_indices_lct.length - 1]].latestCompletionTime());
        if(makespan > tasks[tasks_indices_lct[tasks_indices_lct.length - 1]].latestCompletionTime())
            return false;
        int[] TTEnergy = new int[n];
        Arrays.fill(TTEnergy, 0);
        Timepoint[] est = new Timepoint[n];
        Arrays.fill(est, null);
        for (int j = 0; j < n; j++) {
            int E = 0; int estt = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (tasks[tasks_indices_lct[i]].latestCompletionTime() <= tasks[tasks_indices_lct[j]].latestCompletionTime()) {
                    E += tasks[tasks_indices_lct[i]].energy();
                    if (tasks[tasks_indices_lct[i]].earliestStartingTime() <= estt) {
                        estt = tasks[tasks_indices_lct[i]].earliestStartingTime();
                        est[j] = tasks[tasks_indices_lct[i]].est_to_timepoint;
                    }
                }else {
                    if (tasks[tasks_indices_lct[i]].hasFixedPart() && tasks[tasks_indices_lct[i]].latestStartingTime() < tasks[tasks_indices_lct[j]].latestCompletionTime()) {
                        E += (Math.min(tasks[tasks_indices_lct[j]].latestCompletionTime(), tasks[tasks_indices_lct[i]].earliestCompletionTime()) - tasks[tasks_indices_lct[i]].latestStartingTime())*tasks[tasks_indices_lct[i]].height();
                        if (est[j] != null && tasks[tasks_indices_lct[i]].latestStartingTime() < est[j].time) {
                            est[j] = tasks[tasks_indices_lct[i]].lst_to_timepoint;
                        }
                    }
                }
            }
            TTEnergy[j] = E;
        }
        for(int i=n-1; i > 0; i--)
        {
            if (tasks[tasks_indices_lct[i]].inLambda && tasks[tasks_indices_lct[i]].earliestCompletionTime() < tasks[tasks_indices_lct[i]].latestCompletionTime()) {
                int maxLct = DetectionInitializeIncrements(i);
                DetectionScheduleTasks(maxLct, i);
                int Energy = 0;
                int Est = Integer.MAX_VALUE;
                int estEnergyOverflow = 0;
                boolean isDetected = false;
                int j = 0;
                while (j < n && !isDetected) {
                    if (est[j] != null && tasks[tasks_indices_lct[j]].latestCompletionTime() < tasks[tasks_indices_lct[i]].latestCompletionTime()) {
                        estEnergyOverflow = est[j].energy - est[j].overflow;
                        Timepoint t = tasks[tasks_indices_lct[j]].lct_to_timepoint;
                        int Ei = 0;
                        if (tasks[tasks_indices_lct[i]].hasFixedPart() && tasks[tasks_indices_lct[i]].latestStartingTime() < tasks[tasks_indices_lct[j]].latestCompletionTime())
                            Ei = (Math.min(tasks[tasks_indices_lct[j]].latestCompletionTime(), tasks[tasks_indices_lct[i]].earliestCompletionTime()) - tasks[tasks_indices_lct[i]].latestStartingTime())*tasks[tasks_indices_lct[i]].height();
                        if (t.overflow > t.energy - estEnergyOverflow - TTEnergy[j] + Ei) {
                            Prec[tasks_indices_lct[i]] = j;
                            tasks[tasks_indices_lct[i]].inLambda = false;
                            isDetected = true;
                        }
                    }
                    j++;
                }
                if (isDetected) {
                    int u = 0;
                    while (u < i) {
                        if (tasks[tasks_indices_lct[u]].height() >= tasks[tasks_indices_lct[i]].height() &&
                                tasks[tasks_indices_lct[u]].earliestCompletionTime() >= tasks[tasks_indices_lct[i]].earliestCompletionTime() &&
                                tasks[tasks_indices_lct[u]].earliestStartingTime() <= tasks[tasks_indices_lct[i]].earliestStartingTime()) {
                            if (tasks[tasks_indices_lct[u]].inLambda && tasks[tasks_indices_lct[u]].latestCompletionTime() > tasks[tasks_indices_lct[j-1]].latestCompletionTime()) {
                                Prec[tasks_indices_lct[u]] = j-1;
                                tasks[tasks_indices_lct[u]].inLambda = false;
                            }
                        }
                        u++;
                    }
                } else {
                    int u = 0;
                    while (u < i) {
                        if (tasks[tasks_indices_lct[u]].earliestStartingTime() == tasks[tasks_indices_lct[i]].earliestStartingTime() &&
                                tasks[tasks_indices_lct[u]].height() <= tasks[tasks_indices_lct[i]].height() &&
                                tasks[tasks_indices_lct[u]].earliestCompletionTime() <= tasks[tasks_indices_lct[i]].earliestCompletionTime()) {
                            tasks[tasks_indices_lct[u]].inLambda = false;
                        }
                        u++;
                    }
                }
            }
        }
        return true;
    }
    private int[] EdgeFinder_Pruning()
    {
        for (int i = 0; i < tasks.length; i++)
            estPrime[i] = tasks[i].earliestStartingTime();
        for(int i=0; i<tasks.length; i++)
        {
            if(Prec[i] != -1)
            {
                InitializeIncrements(Prec[i]);
                PruningScheduleTasks(tasks[tasks_indices_lct[Prec[i]]].latestCompletionTime(), tasks[i].height());
                int maxOv = 0;
                if (tasks[i].est_to_timepoint.contact != null) {
                    if (tasks[i].earliestCompletionTime() < tasks[tasks_indices_lct[Prec[i]]].latestCompletionTime()) {
                        if (tasks[tasks_indices_lct[Prec[i]]].lct_to_timepoint.avail - tasks[i].est_to_timepoint.avail < tasks[i].energy())
                            maxOv = tasks[tasks_indices_lct[Prec[i]]].lct_to_timepoint.overlap - tasks[i].est_to_timepoint.contact.overlap - (tasks[tasks_indices_lct[Prec[i]]].lct_to_timepoint.slackUnder - tasks[i].est_to_timepoint.contact.slackUnder);
                        else
                            maxOv = tasks[i].ect_to_timepoint.overlap - tasks[i].est_to_timepoint.contact.overlap - (tasks[tasks_indices_lct[Prec[i]]].lct_to_timepoint.slackUnder - tasks[i].est_to_timepoint.contact.slackUnder);
                    }else {
                        maxOv = tasks[tasks_indices_lct[Prec[i]]].lct_to_timepoint.overlap - tasks[i].est_to_timepoint.contact.overlap - (tasks[tasks_indices_lct[Prec[i]]].lct_to_timepoint.slackUnder - tasks[i].est_to_timepoint.contact.slackUnder);
                    }
                    if(maxOv > 0)
                    {
                        int est = ComputeBound(i, maxOv);
                        if(est > tasks[i].earliestStartingTime())
                        {
                            estPrime[i] = Math.max(estPrime[i], est);
                        }
                    }
                }
            }
        }
        return estPrime;
    }

    private int ComputeBound(int i, int maxOver){
        Timepoint t = tasks[i].est_to_timepoint.contact;
        int est = Integer.MIN_VALUE;
        while(t.next != null){
            int overlap = t.next.overlap - t.overlap;
            if(maxOver > overlap){
                maxOver -= overlap;
                t = t.next;
            }else{
                est = Math.min(t.next.time, t.time + (int)Math.ceil((double)maxOver / (double)(t.cons-(C-tasks[i].height()))));
                return est;
            }
        }
        return est;
    }




    private void PruningScheduleTasks(int maxLCT, int h)
    {
        int hreq, hmaxInc, ov, ect, overlap, slackUnder, slackOver, avail;
        ect = Integer.MIN_VALUE;
        ov = hreq = hmaxInc = overlap = slackUnder = slackOver = avail = 0;
        Timepoint t = tl.first;

        while(t.time < maxLCT)
        {
            int l = t.next.time - t.time;

            hmaxInc += t.incrementMax;
            t.hMaxTotal = hmaxInc;
            int hmax = Math.min(hmaxInc, C);
            hreq += t.increment;
            t.overlap = overlap;
            t.slackUnder = slackUnder;
            t.slackOver = slackOver;
            t.avail = avail;

            int hcons = Math.min(hreq + ov, hmax);
            t.cons = hcons;

            if(ov > 0 && ov < (hcons - hreq) * l)
            {
                l = Math.max(1, ov / (hcons-hreq));
                t.InsertAfter(new Timepoint(t.time + l, t.capacity));
            }
            ov += (hreq - hcons) * l;
            if(hcons > C - h) {
                t.contact = t;
            }


            t.capacity = C - hcons;

            overlap += Math.max(hcons - (C - h), 0) * l;
            slackOver += Math.max(hmax - Math.max(C - h, hcons), 0) * l;
            slackUnder += Math.max(Math.min(C - h, hmax) - hcons, 0) * l;
            avail += Math.min(C-hcons, h) * l;

            if(t.capacity < C)
                ect = t.next.time;

            t = t.next;
        }
        t.overlap = overlap;
        t.slackUnder = slackUnder;
        t.slackOver = slackOver;
        t.avail = avail;
        Timepoint best = null;
        while(t.previous != null){
            if(t.contact != null && best == null)
                best = t.contact;
            if(t.contact != null && t.previous.contact == null)
                t.previous.contact = t.contact;
            if(t.previous.contact != null && best != null){
                if(best.overlap - t.previous.overlap < best.slackUnder - t.previous.slackUnder)
                    t.previous.contact = best;
            }
            t = t.previous;
        }
    }


    private void DetectionScheduleTasks(int maxLCT, int i)
    {
        int hreq, hmaxInc, ov, ect, energy;
        ect = Integer.MIN_VALUE;
        ov = hreq = hmaxInc = energy = 0;
        Timepoint t = tl.first;

        while(t.time < maxLCT)
        {
            int l = t.next.time - t.time;

            hmaxInc += t.incrementMax;
            t.hMaxTotal = hmaxInc;
            int hmax = Math.min(hmaxInc, C);
            hreq += t.increment;
            t.energy = energy;
            t.hreal = hreq;
            t.overflow = ov;

            int hcons = Math.min(hreq + ov, hmax);

            if(ov > 0 && ov < (hcons - hreq) * l)
            {
                l = Math.max(1, ov / (hcons-hreq));
                t.InsertAfter(new Timepoint(t.time + l, t.capacity));
            }
            ov += (hreq - hcons) * l;
            if (tasks[tasks_indices_lct[i]].earliestStartingTime() <= t.time && t.time < tasks[tasks_indices_lct[i]].earliestCompletionTime()) {
                energy += (hreq - tasks[tasks_indices_lct[i]].height()) * l;
            }else {
                energy += hreq * l;
            }

            t.capacity = C - hcons;

            if(t.capacity < C)
                ect = t.next.time;

            t = t.next;
        }
        t.energy = energy;
        t.overflow = ov;
        t.hreal = hreq;
    }

    private int ScheduleTasks(int maxLCT)
    {
        int hreq, hmaxInc, ov, ect;
        ect = Integer.MIN_VALUE;
        ov = hreq = hmaxInc = 0;
        Timepoint t = tl.first;

        while(t.time < maxLCT)
        {
            int l = t.next.time - t.time;

            hmaxInc += t.incrementMax;
            t.hMaxTotal = hmaxInc;
            int hmax = Math.min(hmaxInc, C);
            hreq += t.increment;

            int hcons = Math.min(hreq + ov, hmax);

            if(ov > 0 && ov < (hcons - hreq) * l)
            {
                l = Math.max(1, ov / (hcons-hreq));
                t.InsertAfter(new Timepoint(t.time + l, t.capacity));
            }
            ov += (hreq - hcons) * l;


            t.capacity = C - hcons;

            if(t.capacity < C)
                ect = t.next.time;

            t = t.next;
        }
        if(ov > 0)
            return Integer.MAX_VALUE;

        return ect;
    }
    private int DetectionInitializeIncrements(int u)
    {
        Timepoint t = tl.first;
        int maxLct = Integer.MIN_VALUE;
        while(t != null)
        {
            t.increment = 0;
            t.incrementMax = 0;
            t.hMaxTotal = 0;
            t.hreal = 0;
            t.minimumOverflow = 0;
            t.overflow = 0;
            t.capacity = C;
            t.energy = 0;

            t = t.next;
        }
        int index = -1;
        for(int i = 0; i < u; i++) {
            if (tasks[tasks_indices_lct[i]].latestCompletionTime() < tasks[tasks_indices_lct[u]].latestCompletionTime()) {
                index = -1;
                t = tasks[tasks_indices_lct[i]].est_to_timepoint;
                t.increment += tasks[tasks_indices_lct[i]].height();
                t.incrementMax += tasks[tasks_indices_lct[i]].height();

                t = tasks[tasks_indices_lct[i]].ect_to_timepoint;
                t.increment -= tasks[tasks_indices_lct[i]].height();

                t = tasks[tasks_indices_lct[i]].lct_to_timepoint;
                t.incrementMax -= tasks[tasks_indices_lct[i]].height();
            }
        }
        if (index != - 1) {
            for(int i = index+1; i < n; i++) {
                if (i != u && tasks[tasks_indices_lct[i]].hasFixedPart() && tasks[tasks_indices_lct[i]].latestStartingTime() < tasks[tasks_indices_lct[index]].latestCompletionTime()) {
                    t = tasks[tasks_indices_lct[i]].lst_to_timepoint;
                    t.increment += tasks[tasks_indices_lct[i]].height();
                    t.incrementMax += tasks[tasks_indices_lct[i]].height();
                    if (tasks[tasks_indices_lct[i]].earliestCompletionTime() < tasks[tasks_indices_lct[index]].latestCompletionTime()) {
                        t = tasks[tasks_indices_lct[i]].ect_to_timepoint;
                        t.increment -= tasks[tasks_indices_lct[i]].height();
                        t.incrementMax -= tasks[tasks_indices_lct[i]].height();
                    }else {
                        t = tasks[tasks_indices_lct[index]].lct_to_timepoint;
                        t.increment -= tasks[tasks_indices_lct[i]].height();
                        t.incrementMax -= tasks[tasks_indices_lct[i]].height();
                    }
                }
            }
        }
        t = tasks[tasks_indices_lct[u]].est_to_timepoint;
        t.increment += tasks[tasks_indices_lct[u]].height();
        t.incrementMax += tasks[tasks_indices_lct[u]].height();
        t = tasks[tasks_indices_lct[u]].ect_to_timepoint;
        t.increment -= tasks[tasks_indices_lct[u]].height();
        t.incrementMax -= tasks[tasks_indices_lct[u]].height();
        if (index != -1)
            return tasks[tasks_indices_lct[index]].latestCompletionTime();
        else
            return tasks[tasks_indices_lct[n-1]].latestCompletionTime();
    }



    private void InitializeIncrements(int maxIndex)
    {
        Timepoint t = tl.first;
        while(t != null)
        {
            t.increment = 0;
            t.incrementMax = 0;
            t.hMaxTotal = 0;
            t.hreal = 0;
            t.minimumOverflow = 0;
            t.overflow = 0;
            t.capacity = C;
            t.overlap = 0;
            t.slackOver = 0;
            t.slackUnder = 0;
            t.contact = null;
            t.avail = 0;

            t = t.next;
        }
        for(int i = 0; i <= maxIndex; i++)
        {
            t = tasks[tasks_indices_lct[i]].est_to_timepoint;
            t.increment += tasks[tasks_indices_lct[i]].height();
            t.incrementMax += tasks[tasks_indices_lct[i]].height();

            t = tasks[tasks_indices_lct[i]].ect_to_timepoint;
            t.increment -= tasks[tasks_indices_lct[i]].height();

            t = tasks[tasks_indices_lct[i]].lct_to_timepoint;
            t.incrementMax -= tasks[tasks_indices_lct[i]].height();
        }
    }

    private void InitializeTimeLine()
    {
        int n = tasks.length;
        tl.Add(new Timepoint(tasks[tasks_indices_est[0]].earliestStartingTime(), C));
        Timepoint t = tl.first;

        int p,i,j,k,l;
        p = i = j = k = l = 0;

        int maxLCT = Integer.MIN_VALUE;

        while(i < n || j < n || k < n || l < n)
        {
            if(i<n && (j == n || tasks[tasks_indices_est[i]].earliestStartingTime() <= tasks[tasks_indices_ect[j]].earliestCompletionTime()) &&
                      (k == n || tasks[tasks_indices_est[i]].earliestStartingTime() <= tasks[tasks_indices_lct[k]].latestCompletionTime()) &&
                      (l == n || tasks[tasks_indices_est[i]].earliestStartingTime() <= tasks[tasks_indices_lst[l]].latestStartingTime()))
            {
                if(tasks[tasks_indices_est[i]].earliestStartingTime() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasks[tasks_indices_est[i]].earliestStartingTime(), C));
                    t = t.next;
                }
                tasks[tasks_indices_est[i]].est_to_timepoint = t;
                p += tasks[tasks_indices_est[i]].processingTime();
                maxLCT = Math.max(maxLCT, tasks[tasks_indices_est[i]].latestCompletionTime());

                tasks[tasks_indices_est[i]].inLambda = true;

                i++;
            }
            else if(j < n && (k==n || tasks[tasks_indices_ect[j]].earliestCompletionTime() <= tasks[tasks_indices_lct[k]].latestCompletionTime()) &&
                             (l==n || tasks[tasks_indices_ect[j]].earliestCompletionTime() <= tasks[tasks_indices_lst[l]].latestStartingTime()))
            {
                if(tasks[tasks_indices_ect[j]].earliestCompletionTime() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasks[tasks_indices_ect[j]].earliestCompletionTime(), C));
                    t = t.next;
                }
                tasks[tasks_indices_ect[j]].ect_to_timepoint = t;
                j++;
            }
            else if(k < n && (l == n || tasks[tasks_indices_lct[k]].latestCompletionTime() <= tasks[tasks_indices_lst[l]].latestStartingTime()))
            {
                if (tasks[tasks_indices_lct[k]].latestCompletionTime() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasks[tasks_indices_lct[k]].latestCompletionTime(), C));
                    t = t.next;
                }
                tasks[tasks_indices_lct[k]].lct_to_timepoint = t;
                k++;
            }
            else
            {
                if(tasks[tasks_indices_lst[l]].latestStartingTime() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasks[tasks_indices_lst[l]].latestStartingTime(), C));
                    t = t.next;
                }
                tasks[tasks_indices_lst[l]].lst_to_timepoint = t;
                l++;
            }

        }
        t.InsertAfter(new Timepoint(maxLCT + p, 0));
    }


    /* ------------ Utility Functions --------------*/
    private static Integer[] sortWithJavaLibrary(Task[] tasks, Comparator<Integer> comparator) {

        int n = tasks.length;
        Integer[] tasks_indices = new Integer[n];
        for (int q = 0; q < n; q++) {
            tasks_indices[q] = new Integer(q);
        }
        Arrays.sort(tasks_indices, comparator);
        return tasks_indices;
    }
}
