package br.com.brunoxkk0.helper;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProtectionHandler {

    private static ProtectionHandler protectionHandler;
    private static WorldGuardPlugin worldGuardPluginInstance;

    public static ProtectionHandler getProtectionHandler() {
        return (protectionHandler != null) ? protectionHandler : new ProtectionHandler();
    }

    private ProtectionHandler(){
        protectionHandler = this;

        if((worldGuardPluginInstance = WorldGuardPlugin.inst()) == null){
            throw new NullPointerException();
        }

    }

    public boolean isWorldGuardAvailable(){
        return worldGuardPluginInstance != null;
    }

    public boolean checkFlag(StateFlag defaultFlag, Location location, Player player){

        if(isWorldGuardAvailable()){

            WorldGuardPlugin worldGuardPlugin = WorldGuardPlugin.inst();

            RegionContainer container = worldGuardPlugin.getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(location);

            LocalPlayer localPlayer;

            if((localPlayer = worldGuardPlugin.wrapPlayer(player)) != null){
                return set.allows(defaultFlag, localPlayer);
            }

        }

        throw new NullPointerException();
    }

    public boolean canBuild(Location location, Player player){
        return checkFlag(DefaultFlag.BUILD, location, player);
    }

    public boolean canPvp(Location location, Player player){
        return checkFlag(DefaultFlag.PVP, location, player);
    }

    public boolean canInteract(Location location, Player player){
        return checkFlag(DefaultFlag.INTERACT, location, player);
    }

}
