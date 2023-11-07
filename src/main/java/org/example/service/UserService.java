package org.example.service;

import org.example.domain.Friendship;
import org.example.domain.Tuple;
import org.example.domain.User;
import org.example.repository.InMemoryRepository;
import org.example.repository.Repository;

import java.util.*;
import java.util.random.RandomGenerator;

public class UserService implements Service{
    private final Repository<Long, User> userRepository;

    public UserService(InMemoryRepository<Long, User> repository){
        this.userRepository = repository;
    }
    /**
     * Construieste un String cu informatiile relevante a tuturor utilizatorilor
     * @return
     */

    public String getEntities() {
        Iterable<User> iterable = userRepository.findAll();
        StringBuilder string = new StringBuilder();

        for(User user : iterable){
            string.append(user.toString()).append(" id : ").append(user.getId()).append("\n");
        }

        return string.toString();
    }

    /**
     * Construieste un String cu informatiile relevante a tuturor prieteniilor utilizatorilor
     * @return String
     */
    public String getFriendships(){
        StringBuilder string = new StringBuilder();
        Iterable<User> users = userRepository.findAll();
        for( User user : users){
            string.append(user.toString()).append(user.getFriends().toString()).append("\n");
        }
        return string.toString();
    }

    /**
     * Sterge un utilizator din repo
     * @param id id-ul utilizatorului
     * @return String numele utilizatorului sters
     */
    public Object removeEntity(Long id) {
        Optional<User> u = userRepository.delete(id);

        if(u.isPresent()){
            User deletedUser = u.get();
            for(User user: userRepository.findAll()){
                user.removeFriend(deletedUser);
            }
        }
        return u.orElse(null);
    }

    /**
     * Adauga un utilizator in repo
     * @param user utilizatorul
     */
    public void addUser(User user){
        user.setId(RandomGenerator.getDefault().nextLong());
        userRepository.save(user);
    }

    /**
     * Adauga prietenia dintre doi utlizatori
     * @param id1 id utilizator1
     * @param id2 id utilizator2
     */
    public void addFriendship(Long id1, Long id2){
        Optional<User> user1 = userRepository.findOne(id1);
        Optional<User> user2 = userRepository.findOne(id2);
        if (user1.isPresent() && user2.isPresent()){
            user1.get().addFriend(user2.get());
            user2.get().addFriend(user1.get());
        }
    }

    /**
     * Elimina prietenia dintre doi utilizatori
     * @param id1 id utilizator1
     * @param id2 id utilizator2
     */
    public void removeFriendship(Long id1, Long id2){
        Optional<User> user1 = userRepository.findOne(id1);
        Optional<User> user2 = userRepository.findOne(id2);
        if(user1.isPresent() && user2.isPresent()){
            user2.get().removeFriend(user1.get());
            user1.get().removeFriend(user2.get());
        }
    }

    /**
     * Calculeaza numarul de comunitati
     * @return int
     */
    public int getNumberOfCommunities(){
        ArrayList<Friendship> friendships = getFriendshipList();
        return noConnectedComponents(friendships);
    }

    /**
     * Apeleaza functia mostSociableCommunity si construieste un string cu datele relevante
     * @return String
     */
    /**
     * Genereaza instantele de prietenii intre utilizatori
     * @return ArrayList<Friendship> lista de prietenii
     */
    private ArrayList<Friendship> getFriendshipList(){
        ArrayList<Friendship> friendships = new ArrayList<>();


        for(User user : userRepository.findAll()){
            for( User friend : user.getFriends()){
                Friendship friendship = new Friendship();
                friendship.setId(new Tuple<Long, Long>(user.getId(), friend.getId()));
                friendships.add(friendship);
            }
        }
        return friendships;
    }

    /**
     * Gaseste comunitatea cu cele mai multe utilizatori
     *
     * @param friendships ArrayList<Friendship> lista de prietenii
     * @return ArrayList<Long> lista de id a utilizatorilor din comunitate
     */

    /**
     * Calculeaza numarul de comunitati
     * @param friendships Lista de prietenii
     * @return int numarul de comunitati
     */
    private int noConnectedComponents(ArrayList<Friendship> friendships) {
        Map<Long, ArrayList<Long>> graph = new HashMap<>();
        createGraph(friendships, graph);
        Set<Long> visited = new HashSet<>();
        int components = 0;

        // Verifică dacă există noduri izolate (fără prieteni) și le adaugă la numărul de componente
        Set<Long> isolatedNodes = new HashSet<>();
        for (User user : userRepository.findAll()) {
            if (!graph.containsKey(user.getId())) {
                isolatedNodes.add(user.getId());
            }
        }

        components += isolatedNodes.size();

        for (Long node : graph.keySet()) {
            if (!visited.contains(node)) {
                components++;
                dfs(node, visited, graph, false, new ArrayList<>());
            }

        }
        return components;
    }

    /**
     * Genereaza graful corespunzator prieteniilor intre utilizatori
     * @param friendships Lista de prietenii
     * @param graph Graful care se construieste
     */
    public static void createGraph(ArrayList<Friendship> friendships, Map<Long, ArrayList<Long>> graph){
        if(friendships == null || friendships.isEmpty()){
            return; // No friendships means no connected components.
        }
        // Iterate through each friendship tuple and build the graph.
        for(Friendship friendship : friendships){
            Long person1 = friendship.getId().getLeft();
            Long person2 = friendship.getId().getRight();

            // Assuming undirected friendships, add edges in both directions.
            addEdge(person1, person2, graph);
            addEdge(person2, person1, graph);
        }
    }

    private static void addEdge(Long source, Long destination, Map<Long, ArrayList<Long>> graph) {
        graph.computeIfAbsent(source, k -> new ArrayList<>()).add(destination);
    }

    private static void dfs(Long node, Set<Long> visited, Map<Long, ArrayList<Long>> graph, boolean saveComponent, ArrayList<Long> currentComponent) {
        visited.add(node);

        if (graph.containsKey(node)) {
            for (Long neighbor : graph.get(node)) {
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, visited, graph, saveComponent, currentComponent);
                }
            }
        }
        if (saveComponent)
            currentComponent.add(node);
    }



    public String getMostSocialCommunity() {
        Map<Long, ArrayList<Long>> graph = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Friendship> friendships = getFriendshipList();
        createGraph(friendships, graph);
        List<Long> mostSocialCommunity = findLongestPath(graph);

        for(Long id : mostSocialCommunity){
            Optional<User> user = userRepository.findOne(id);
            user.ifPresent(value -> stringBuilder.append(value.toString()).append(" "));
        }
        return stringBuilder.toString();
    }

    private List<Long> findLongestPath(Map<Long, ArrayList<Long>> graph) {
        List<Long> longestPath = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        for (Long node : graph.keySet()) {
            List<Long> currentPath = new ArrayList<>();
            dfsForLongestPath(node, visited, graph, currentPath, longestPath);

        }

        return longestPath;
    }

    private void dfsForLongestPath(Long node, Set<Long> visited, Map<Long, ArrayList<Long>> graph, List<Long> currentPath, List<Long> longestPath) {
        visited.add(node);
        currentPath.add(node);

        if (graph.containsKey(node)) {
            for (Long neighbor : graph.get(node)) {
                if (!visited.contains(neighbor)) {
                    dfsForLongestPath(neighbor, visited, graph, currentPath, longestPath);
                }
            }
        }

        if (currentPath.size() > longestPath.size()) {
            longestPath.clear();
            longestPath.addAll(currentPath);
        }

        currentPath.remove(node);
    }

}