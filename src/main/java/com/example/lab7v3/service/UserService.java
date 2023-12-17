package com.example.lab7v3.service;

import com.example.lab7v3.domain.Friendship;
import com.example.lab7v3.domain.Tuple;
import com.example.lab7v3.domain.User;
import com.example.lab7v3.observer.*;
import com.example.lab7v3.observer.Observable;
import com.example.lab7v3.observer.Observer;
import com.example.lab7v3.repository.FriendshipDBRepository;
import com.example.lab7v3.repository.Repository;
import com.example.lab7v3.repository.UserDBRepository;
import com.example.lab7v3.repository.paging.Page;
import com.example.lab7v3.repository.paging.Pageable;
import com.example.lab7v3.repository.paging.PagingRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserService implements Service, Observable<UserChangeEvent> {

    private final PagingRepository<Long, User> userRepository;

    private final List<Observer<UserChangeEvent>> observers = new ArrayList<>();
    private final Repository<Tuple<Long, Long>, Friendship> friendshipRepository;

    public Page<User> findAllOnPage(Pageable pageable){
        return userRepository.findAllOnPage(pageable);
    }

    public UserService(UserDBRepository userRepository, FriendshipDBRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
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

    public Iterable<User> getAll(){
        return userRepository.findAll();
    }

    /**
     * Construieste un String cu informatiile relevante a tuturor prieteniilor utilizatorilor
     * @return String
     */
    public String getFriendships(){
        StringBuilder string = new StringBuilder();
        Iterable<Friendship> friendships = friendshipRepository.findAll();
        for( Friendship friendship : friendships){
            Long userId1 = friendship.getId().getLeft();
            Long userId2 = friendship.getId().getRight();

            Optional<User> user1 = userRepository.findOne(userId1);
            Optional<User> user2 = userRepository.findOne(userId2);

            if(user1.isPresent() && user2.isPresent()){
                string.append(user1.get().toString())
                        .append(" and ")
                        .append(user2.get().toString())
                        .append(" became friends on ")
                        .append(friendship.getFriendsFrom())
                        .append("\n");
            }
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
            notifyAll(new UserChangeEvent(UserChangeEventType.DELETE, deletedUser, null));
        }
        return u.orElse(null);
    }

    /**
     * Adauga un utilizator in repo
     * @param user utilizatorul
     */
    public Optional<User> addUser(User user){
        user.setId(RandomGenerator.getDefault().nextLong());
        Optional<User> opt = userRepository.save(user);

        if(opt.isEmpty())
            notifyAll(new UserChangeEvent(UserChangeEventType.ADD, null, user));
        return opt;
    }

    public Optional<User> update(User user){
        Optional<User> optOldUser = userRepository.findOne(user.getId());

        if(optOldUser.isPresent()) {
            Optional<User> opt = userRepository.update(user);

            if(opt.isEmpty())
                notifyAll(new UserChangeEvent(UserChangeEventType.UPDATE, optOldUser.get(), user));

            return Optional.empty();
        }
        return Optional.of(user);
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
            Friendship newFriendship = new Friendship();
            newFriendship.setId(new Tuple<>(user1.get().getId(), user2.get().getId()));
            newFriendship.setFriendsFrom(LocalDateTime.now());

            friendshipRepository.save(newFriendship);
        }
    }

    /**
     * Elimina prietenia dintre doi utilizatori
     * @param id1 id utilizator1
     * @param id2 id utilizator2
     */
    public void removeFriendship(Long id1, Long id2) {
        Optional<User> user1 = userRepository.findOne(id1);
        Optional<User> user2 = userRepository.findOne(id2);

        if (user1.isPresent() && user2.isPresent()) {
            // Try to find the friendship in both directions
            Optional<Friendship> friendship1 = friendshipRepository.findOne(new Tuple<>(id1, id2));
            Optional<Friendship> friendship2 = friendshipRepository.findOne(new Tuple<>(id2, id1));

            // Delete the friendship if found in either direction
            friendship1.ifPresent(friendship -> friendshipRepository.delete(friendship.getId()));
            friendship2.ifPresent(friendship -> friendshipRepository.delete(friendship.getId()));
        }
    }


    /**
     * Calculeaza numarul de comunitati
     * @return int
     */
    public int getNumberOfCommunities(){
        ArrayList<Friendship> friendships = new ArrayList<>();
        Iterable<Friendship> allFriendships = friendshipRepository.findAll();

        allFriendships.forEach(friendships::add);

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
    private List<Friendship> getFriendshipList() {
        List<Friendship> friendships = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            Long userId = user.getId();
            List<User> friends = user.getFriends();

            for (User friend : friends) {
                Long friendId = friend.getId();

                // Assuming you have a FriendshipRepository with a method to find friendships by user IDs
                friendshipRepository.findOne(new Tuple<>(userId, friendId))
                        .ifPresent(friendships::add);
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
        ArrayList<Friendship> friendships = new ArrayList<>();
        Iterable<Friendship> allFriendships = friendshipRepository.findAll();

        allFriendships.forEach(friendships::add);

        createGraph(friendships, graph);
        List<Long> mostSocialCommunity = findLongestPath(graph);

        for (Long id : mostSocialCommunity) {
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

    public String getFriendshipsByMonth(Long userId, Month month) {
        Optional<User> user = userRepository.findOne(userId);

        if (user.isPresent()) {
            Iterable<Friendship> friendships = friendshipRepository.findAll();
            return StreamSupport.stream(friendships.spliterator(), false)
                    .filter(friendship -> {
                        Tuple<Long, Long> friendshipIds = friendship.getId();
                        return (friendshipIds.getLeft().equals(userId) || friendshipIds.getRight().equals(userId))
                                && friendship.getFriendsFrom().getMonth() == month;
                    })
                    .map(friendship -> {
                        Long friendId = friendship.getId().getLeft().equals(userId) ?
                                friendship.getId().getRight() : friendship.getId().getLeft();
                        Optional<User> friend = userRepository.findOne(friendId);
                        return friend.map(value ->
                                        user.get().getLastName() + " " + user.get().getFirstName() +
                                                " | " + value.getLastName() + " " + value.getFirstName() +
                                                " | " + friendship.getFriendsFrom())
                                .orElse("");
                    })
                    .collect(Collectors.joining("\n"));
        } else {
            return "Utilizatorul cu ID-ul " + userId + " nu există.";
        }
    }

    @Override
    public void notifyAll(UserChangeEvent userChangeEvent) {
        observers.forEach(o -> o.update(userChangeEvent));
    }

    @Override
    public void addObserver(Observer<UserChangeEvent> obs) {
        observers.add(obs);
    }

    @Override
    public void removeObserver(Observer<UserChangeEvent> obs) {
        observers.remove(obs);
    }

}