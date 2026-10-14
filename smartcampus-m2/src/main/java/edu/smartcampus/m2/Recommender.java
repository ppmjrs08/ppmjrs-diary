package edu.smartcampus.m2;

import java.util.*;

/** Content-based English TF-IDF; eligibility rules are separate from ranking. */
public class Recommender {
    public record Match(Course course, double score, List<String> matchingTerms, String reason) {}
    private static final Set<String> STOP = Set.of("a","an","the","and","or","to","of","in","for","i","want","learn","about","with","my");

    public List<Match> rank(List<Course> catalog, String query, Set<String> completed, int topK) {
        validate(query, topK);
        List<Course> active = catalog.stream().filter(Course::active).toList();
        Map<String, Integer> df = new TreeMap<>();
        for (Course c : active) tokens(c.title()+" "+c.description()).keySet().forEach(t->df.merge(t,1,Integer::sum));
        Map<String, Double> idf = new TreeMap<>();
        df.forEach((t,n)->idf.put(t, Math.log((1.0+active.size())/(1.0+n))+1.0));
        Map<String, Double> q = vector(query,idf);
        List<Match> matches = new ArrayList<>();
        for (Course c:active) {
            if (!eligible(c,completed)) continue;
            Map<String,Double> v=vector(c.title()+" "+c.description(),idf);
            double score=0; List<String> shared=new ArrayList<>();
            for (var term:q.entrySet()) {
                double contribution=term.getValue()*v.getOrDefault(term.getKey(),0.0);
                score+=contribution; if(contribution>0) shared.add(term.getKey());
            }
            if(score>0) matches.add(match(c,Math.min(1.0,score),shared));
        }
        return sorted(matches,topK);
    }

    public List<Match> baseline(List<Course> catalog, String query, Set<String> completed, int topK) {
        validate(query,topK); Set<String> words=tokens(query).keySet(); List<Match> matches=new ArrayList<>();
        for(Course c:catalog) {
            if(!eligible(c,completed)) continue;
            var common=new TreeSet<>(tokens(c.title()+" "+c.description()).keySet()); common.retainAll(words);
            if(!common.isEmpty()) matches.add(match(c, (double)common.size()/Math.max(1,words.size()),new ArrayList<>(common)));
        }
        return sorted(matches,topK);
    }
    private boolean eligible(Course c,Set<String> completed) { return c.active()&&!completed.contains(c.id())&&completed.containsAll(c.prerequisites()); }
    private Match match(Course c,double score,List<String> shared) {
        String prerequisites=c.prerequisites().isEmpty()?"No prerequisites":"Completed prerequisites: "+String.join(", ",new TreeSet<>(c.prerequisites()));
        return new Match(c,score,List.copyOf(shared),"Matching terms: "+String.join(", ",shared)+". "+prerequisites+".");
    }
    private List<Match> sorted(List<Match> list,int k) {
        list.sort(Comparator.comparingDouble(Match::score).reversed().thenComparing(m->m.course().id()));
        return List.copyOf(list.subList(0,Math.min(k,list.size())));
    }
    private void validate(String query,int k) {
        if(query==null||query.isBlank()||query.length()>801) throw new IllegalArgumentException("Enter interests and goals within the supported length");
        if(k<1||k>3) throw new IllegalArgumentException("topK must be between 1 and 3");
    }
    private Map<String,Integer> tokens(String text) {
        Map<String,Integer> map=new TreeMap<>();
        for(String t:text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) if(!t.isEmpty()&&!STOP.contains(t)) map.merge(t,1,Integer::sum);
        return map;
    }
    private Map<String,Double> vector(String text,Map<String,Double> idf) {
        Map<String,Double> v=new TreeMap<>(); double sum=0;
        for(var t:tokens(text).entrySet()) if(idf.containsKey(t.getKey())) {
            double weight=t.getValue()*idf.get(t.getKey()); v.put(t.getKey(),weight); sum+=weight*weight;
        }
        double norm=Math.sqrt(sum); if(norm>0) v.replaceAll((t,w)->w/norm); return v;
    }
}
