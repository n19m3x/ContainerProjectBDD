package application;

import java.util.*;
import java.util.stream.*;

import application.data.QueryHashSet;
import application.data.QueryLinkedList;
import application.data.SingletonPortsHashSet;
import application.models.Client;
import application.models.Container;
import application.models.Journey;
import application.models.LogisticCompany;
import application.models.Pair;
import application.models.Port;
import application.models.User;

public class ContainerApp {
	
	private static ContainerApp instance = null;
	
	public static ContainerApp getInstance() {
		if(instance == null) {
			instance = new ContainerApp();
		}
		return instance;
	}
	
	public void clearPorts() {
		SingletonPortsHashSet.getInstance().clear();
	}
	
//	private QueryHashSet<Port>...;
	private QueryHashSet<User> users = new QueryHashSet<User>();
	private QueryLinkedList<Container> containers = new QueryLinkedList<Container>();
	private QueryLinkedList<Journey> journeys = new QueryLinkedList<Journey>();
	
	public void registerClient(String clientName, String address, String contactPerson, String email, String password) throws Exception {
		if (!users.add(new Client(clientName, address, contactPerson, email, password))) {
			throw new Exception("Client already registered");
		}
	}
	
	public User loginUser(String username, String password) throws Exception {
		Optional<User> user = users.stream().filter((user1)->user1.get("clientName").equals(username)).findFirst();
		if (user.isEmpty()) {
			throw new Exception("Username is incorrect");
		}
		else if (!user.get().get("password").equals(password)) {
			throw new Exception("Password is incorrect");
		}
		else {
			return user.get();
		}
	}
	
	public List<User> findClient(String... keywords) throws Exception {
		List<User> resultingClients =  users.findElements(keywords);
		if (resultingClients.isEmpty()) {
			throw new Exception("No clients found");

		}
		return resultingClients;
	}


	public void updateClient(User client, String key, String value) throws Exception {
		if (key.equals("clientName")) {
			if (isClientAvailable(value)) {
				client.setClientInfo(key, value);
			}
			else {
				throw new Exception("New client name already exists");
			}
		} else {
			client.setClientInfo(key, value);
		}
	}

	private boolean isClientAvailable(String clientName) {
		return users.stream().noneMatch((client)->client.get("clientName").equals(clientName));
	}



	public void registerPort(String port) throws Exception {
		if (!SingletonPortsHashSet.getInstance().add(new Port(port))) {
			throw new Exception("Port is already registered");
		}
	}

	private boolean portIsRegistered(String port) {
		return SingletonPortsHashSet.getInstance().contains(new Port(port));
		
	}
	

	public Port findPort(String port){
		return SingletonPortsHashSet.getInstance().findElement(port);
	}
	
	public List<Port> findPorts(String port){
		return SingletonPortsHashSet.getInstance().findElements(port);
	}
	
	public void createContainer(String port) throws Exception {
		if (!portIsRegistered(port)) {
			throw new Exception("Port is not registered");
		}
		Port p = findPort(port);
		Container container = new Container(p);
		containers.add(container);
		p.addContainer(container);
		
	}

	public void registerContainer(String portOfOrigin, String destination, String content, Client client) throws Exception {
		Port startport = findPort(portOfOrigin);
		Port finalport = findPort(destination);
		
		if (startport == null || finalport == null) {
			throw new Exception ("No valid ports");
		}
		
		Container availableContainer = getAvailableContainer(startport);
		if(availableContainer == null) {
			throw new Exception ("No available containers in port");
		}
		
		Journey journey = new Journey (startport, finalport, content, client);
		journeys.add(journey);
		availableContainer.setJourney(journey);
		availableContainer.getJourneys().add(journey);
		client.getClientContainers().add(availableContainer);
				
	}

	private Container getAvailableContainer(Port startport) {
		return containers.stream().filter((container)->container.isContainerAvailable(startport)).findFirst().orElse(null);
	}

	public List<Container> findContainer(String... keywords) throws Exception {
		List<Container> resultingContainers = containers.findElements(keywords);
		if (resultingContainers.isEmpty()) {
			throw new Exception("No containers found");

		}
		return resultingContainers;
	}


	public List<Journey> findJourney(String... keywords) throws Exception {
		List<Journey> resultingJourneys = journeys.findElements(keywords);
		if (resultingJourneys.isEmpty()) {
			throw new Exception("No journeys found");
			
		}
		
		return resultingJourneys;
	}

	public void updateJourney(Container container, List<String> times, List<String> locations, List<Integer> temperatures, List<Integer> humidities, List<Integer> pressures) throws Exception {
		if (!container.hasJourney()) {
			throw new Exception ("Container is not on a journey");
		}
		if (isLocationNotValid(locations)) {
			throw new Exception ("Location is not valid");
		}
		container.updateJourney(times, convertLocations(locations), temperatures, humidities, pressures);
	}

	private List<Port> convertLocations(List<String> locations) {
		return locations.stream().map((location)->findPort(location)).collect(Collectors.toList());
	}

	private boolean isLocationNotValid(List<String> locations) {
		return locations.stream().anyMatch((location)->!portIsRegistered(location));
	}
	
	
	
	
	
	

	public Pair<Container,Integer> mostKilometersTraveled() throws Exception {
		if (!containers.isEmpty()) {
			Container container = Collections.max(containers,Comparator.comparing(c -> c.getDistance()));
			return new Pair<Container,Integer>(container,container.getDistance());
		}
		throw new Exception("No containers exist");
	}

	public Pair<Container,Integer> mostJourneys() throws Exception {
		if (!containers.isEmpty()) {
			Container container = Collections.max(containers,Comparator.comparing(c -> c.getNumberOfJourneys()));
			return new Pair<Container,Integer>(container,container.getDistance());
		}
		throw new Exception("No containers exist");
	}
	
	public Pair<Container,Integer> mostPorts() throws Exception {
		if (!containers.isEmpty()) {
			Container container = Collections.max(containers,Comparator.comparing(c -> c.getNumberOfPorts()));
			return new Pair<Container,Integer>(container,container.getDistance());
		}
		throw new Exception("No containers exist");
	}

	public Pair<Container,Integer> leastKilometersTraveled() throws Exception {
		if (!containers.isEmpty()) {
			Container container = Collections.min(containers,Comparator.comparing(c -> c.getDistance()));
			return new Pair<Container,Integer>(container,container.getDistance());
		}
		throw new Exception("No containers exist");		
	}

	public Pair<Container,Integer> leastJourneys() throws Exception {
		if (!containers.isEmpty()) {
			Container container = Collections.min(containers,Comparator.comparing(c -> c.getNumberOfJourneys()));
			return new Pair<Container,Integer>(container,container.getDistance());
		}
		throw new Exception("No containers exist");			
	}

	public Pair<Container,Integer> leastPorts() throws Exception {
		if (!containers.isEmpty()) {
			Container container = Collections.min(containers,Comparator.comparing(c -> c.getNumberOfPorts()));
			return new Pair<Container,Integer>(container,container.getDistance());
		}
		throw new Exception("No containers exist");			
	}

	public Pair<Journey,Integer> longestJourney() throws Exception {
		if (!journeys.isEmpty()) {
			Journey journey = Collections.max(journeys,Comparator.comparing(j -> j.getDistance()));
			return new Pair<Journey,Integer>(journey,journey.getDistance());
			
		}
		throw new Exception("No journeys exist");			
	}
	public Pair<Journey,Integer> shortestJourney() throws Exception {
		if (!journeys.isEmpty()) {
			Journey journey = Collections.min(journeys,Comparator.comparing(j -> j.getDistance()));
			return new Pair<Journey,Integer>(journey,journey.getDistance());
		}
		throw new Exception("No journeys exist");			
	}

	
	public QueryLinkedList<Container> getContainers() {
		return containers;
	}
	
	
}

