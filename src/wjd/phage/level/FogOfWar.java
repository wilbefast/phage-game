/*
 Copyright (C) 2013 William James Dyce

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package wjd.phage.level;

import java.util.LinkedList;
import java.util.List;
import wjd.math.Circle;
import wjd.math.M;
import wjd.math.V2;
import wjd.phage.unit.Unit;

/**
 *
 * @author wdyce
 * @since Jan 14, 2013
 */
public class FogOfWar 
{
  /* ATTRIBUTES */
  public TileGrid grid;
  private Circle tileFlare = new Circle();
  private List<Circle> flares = new LinkedList<Circle>();
  
  /* METHODS */
  
  // constructors
  
  public FogOfWar(TileGrid grid_)
  {
    this.grid = grid_;
  }
  
  // accessors
  
  public void recalculate()
  {
    flares.clear();
    
    for(Tile t : grid)
    {
      // shroud everything
      t.setVisibility(Tile.EVisibility.UNSEEN);
      
      // build a list of units that can see
      Unit u = t.getUnit();
      if(u == null) 
        continue;
      Circle f = u.getSight();
      if(f == null) 
        continue;
      flares.add(f);
    }
    
    // reveal areas that are in sight
    for(Circle f : flares)
      reveal(f);
      
  }
  
  public void reveal(Circle flare)
  {
    
    tileFlare.reset(flare).mult(Tile.ISIZE).centre.floor();

    //"defog" those part of the view mask that are in view
    castLightV(-1, 0, -1, 0, tileFlare.centre); //NNW
    //castLightV(0, 1, -1, 0, tileFlare.centre); //NNE
    //castLightH(-1, 0, 1, 0, tileFlare.centre); //NEE
    //castLightH(0, 1, 1, 0, tileFlare.centre); //SEE
    castLightV(0, 1, 1, 0, tileFlare.centre); //SSE
    /*castLightV(-1, 0, 1, 0, tileFlare.centre); //SSW
    castLightH(0, 1, -1, 0, tileFlare.centre); //SWW
    castLightH(-1, 0, -1, 0, tileFlare.centre); //NWW*/
  }
  
  /* SUBROUTINES */

  // Cast light, iterating x across y
  private void castLightV(float slopeA, float slopeB, int side, int width, V2 tilePosStart)
  {
    // if the source is inside a wall stop immediately
    Tile source_tile = grid.gridToTile(tilePosStart);
    if(source_tile == null || source_tile.getType() == Tile.ETerrain.WALL)
      return;
    boolean inWall = false;
    
    // calculate which of the slopes if greater
    float minSlope = Math.min(slopeA, slopeB), 
          maxSlope = Math.max(slopeA, slopeB);

    // iterate x across y
    V2 tilePos = tilePosStart.clone();
    for(int i = 0; 
        !inWall && ((tilePos.y - tileFlare.centre.y)*side < tileFlare.radius); 
        tilePos.y += side)
    {
      // get the initial Tile
      tilePos.x = (int)(Math.max(0, tilePosStart.x + minSlope*i - width));
      if(!grid.validGridPos(tilePos))
        break;
      Tile revealTile = grid.gridToTile(tilePos);
      int first_x = (int)tilePos.x;
      
      // detected row of blocks doesn't lap around to the next row, so reset!
      inWall = (revealTile.getType() == Tile.ETerrain.WALL);

      // count the number of cells passed since last block
      int free_cells = 0; 
      for( ; 
        grid.validGridPos(tilePos) && (tilePos.x <= tilePosStart.x + maxSlope*i); 
        tilePos.x++)
      {
        // ignore Tiles outside the reveal circle
        if(tileFlare.contains(tilePos))
        {
          // reveal the current Tile
          revealTile = grid.gridToTile(tilePos);
          revealTile.setVisibility(Tile.EVisibility.VISIBLE);

          // is the current Tile a wall?
          if(revealTile.getType() == Tile.ETerrain.WALL)
          {
            // if so this is the start of a row of blockers...
            if(!inWall && tilePos.x - 1 >= first_x)
            {
              // ... start a child scan, continue with the current one
              V2 newStart = tilePos.clone().add(-1, 0);
              float newSlope = side * (newStart.x - tilePosStart.x) 
                                    / (newStart.y - tilePosStart.y);

              castLightV((float)M.maxAbs(minSlope, maxSlope), 
                         newSlope, 
                         side, 
                         Math.max(0, free_cells - 1), 
                         newStart);
              
              // break!
              inWall = true;
            }
            free_cells = 0;
          }
          // otherwise the current Tile is free
          else
          {
            // this is the end of a row of blockers...
            if(inWall)
            {
              // ...recalibrate the current scan's slopes
              float newSlope = -side * (tilePos.x - tilePosStart.x)
                                      / (tilePosStart.y-tilePos.y);
              minSlope = newSlope;
              width = 0;
              
              // unbreak!
              inWall = false;
            }
            free_cells++;
          }
        }
      }
      i++;
    }
  }
  
  // Cast light, iterating y across x
  private void castLightH(float slopeA, float slopeB, int side, int width, V2 tilePosStart)
  {
    // if the source is inside a wall stop immediately
    Tile source_tile = grid.gridToTile(tilePosStart);
    if(source_tile == null || source_tile.getType() == Tile.ETerrain.WALL)
      return;
    boolean inWall = false;
    
    // calculate which of the slopes if greater
    float minSlope = Math.min(slopeA, slopeB), 
          maxSlope = Math.max(slopeA, slopeB);

    // iterate y across x
    V2 tilePos = tilePosStart.clone();
    for(int i = 0; 
        !inWall && ((tilePos.x - tileFlare.centre.x)*side < tileFlare.radius); 
        tilePos.x += side)
    {
      // get the initial Tile
      tilePos.y = (int)(Math.max(0, tilePosStart.y + minSlope*i - width));
      if(!grid.validGridPos(tilePos))
        break;
      Tile revealTile = grid.gridToTile(tilePos);
      int first_y = (int)tilePos.y;
      
      // detected row of blocks doesn't lap around to the next row, so reset!
      inWall = (revealTile.getType() == Tile.ETerrain.WALL);

      // count the number of cells passed since last block
      int free_cells = 0; 
      for( ; 
        grid.validGridPos(tilePos) && (tilePos.y <= tilePosStart.y + maxSlope*i); 
        tilePos.y++)
      {
        // ignore Tiles outside the reveal circle
        if(tileFlare.contains(tilePos))
        {
          // reveal the current Tile
          revealTile = grid.gridToTile(tilePos);
          revealTile.setVisibility(Tile.EVisibility.VISIBLE);

          // is the current Tile a wall?
          if(revealTile.getType() == Tile.ETerrain.WALL)
          {
            // if so this is the start of a row of blockers...
            if(!inWall && tilePos.y - 1 >= first_y)
            {
              // ... start a child scan, continue with the current one
              V2 newStart = tilePos.clone().add(0, -1);
              float newSlope = side * (newStart.y - tilePosStart.y) 
                                    / (newStart.x - tilePosStart.x);

              castLightH((float)Math.min(minSlope, maxSlope), 
                         newSlope, 
                         side, 
                         Math.max(0, free_cells - 1), 
                         newStart);
              // break!
              inWall = true;
            }
            free_cells = 0;
          }
          // otherwise the current Tile is free
          else
          {
            // this is the end of a row of blockers...
            if(inWall)
            {
              // ...recalibrate the current scan's slopes
              float newSlope = -side * (tilePos.y - tilePosStart.y)
                                      / (tilePosStart.x - tilePos.x);
              minSlope = newSlope;
              width = 0;
              
              // unbreak!
              inWall = false;
            }
            free_cells++;
          }
        }
      }
      i++;
    }
  }
}
