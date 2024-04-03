package Server;

import java.util.ArrayList;

public class Group {
    private ArrayList<Person> persons;
    private String groupName;

    public Group(String newName){
        persons = new ArrayList<>();
        groupName = newName;

    }
    public void removePersonFromGroup(Person p){
        persons.remove(p);
    }
    public void addPersonToGroup(Person newPerson){
        persons.add(newPerson);
    }
    public ArrayList<Person> getPersons(){
        return persons;
    }
    public void deletePersonFromGroup(Person newPerson){
        persons.remove(newPerson);
    }
    public String getGroupName(){
        return groupName;
    }
    
}
