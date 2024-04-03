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
    
}
