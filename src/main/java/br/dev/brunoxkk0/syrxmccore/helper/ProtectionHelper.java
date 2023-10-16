package br.dev.brunoxkk0.syrxmccore.helper;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProtectionHelper {

    private static ProtectionHelper protectionHelper;
    private static WorldGuardPlugin worldGuardPluginInstance;

    public static ProtectionHelper getProtectionHandler() {
        return (protectionHelper != null) ? protectionHelper : new ProtectionHelper();
    }

    private ProtectionHelper(){
        protectionHelper = this;

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
            ApplicableRegionSet regions = query.getApplicableRegions(location);

            LocalPlayer localPlayer;

            if((localPlayer = worldGuardPlugin.wrapPlayer(player)) != null){
                return regions.allows(defaultFlag, localPlayer);
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
