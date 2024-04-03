package Server;
import java.util.ArrayList;

import Client.*;

public class GroupController {
    private ArrayList<Group> groups;
    public GroupController(ArrayList<Group> list){
        this.groups = list;
    }
    public Boolean groupExists(String groupName){
        Boolean result = false;
        for(int i=0;i<groups.size();i++){
            if(groups.get(i).getGroupName().equals(groupName)){
                result = true;
                break;
            }
        }
        return result;
    }
    public ArrayList<Group> getGroups(){
        return groups;
    }
    public void addClientToGroup(int groupIndex,Person p){
        Group g= groups.get(groupIndex);
        g.addPersonToGroup(p);
        p.addToGroup(g);
        p.setIsInGroup(true);
    }
    public String listGroups(){
        String msj = "\n CREATED GROUPS :";
        for(int i=0;i<groups.size();i++){
            msj+="\n"+groups.get(i).getGroupName();
        }
        return msj;
    }
    public int searchGroup(String name){
        int index = -1;
        for(int i=0;i<groups.size();i++){
            if(groups.get(i).getGroupName().equals(name)){
                index = i;
            } 
        }
        return index;

    }
    
}
