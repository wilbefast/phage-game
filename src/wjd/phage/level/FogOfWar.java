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

import wjd.math.Circle;
import wjd.math.M;
import wjd.math.V2;

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
  
  /* METHODS */
  
  // constructors
  
  public FogOfWar(TileGrid grid_)
  {
    this.grid = grid_;
  }
  
  // accessors
  
  public void reveal(Circle flare)
  {
    
    tileFlare.reset(flare).mult(Tile.ISIZE);

    //"defog" those part of the view mask that are in view
    castLightV(-1, 0, -1, 0, tileFlare.centre); //NNW
    castLightV(0, 1, -1, 0, tileFlare.centre); //NNE
    castLightH(-1, 0, 1, 0, tileFlare.centre); //NEE
    castLightH(0, 1, 1, 0, tileFlare.centre); //SEE
    castLightV(0, 1, 1, 0, tileFlare.centre); //SSE
    castLightV(-1, 0, 1, 0, tileFlare.centre); //SSW
    castLightH(0, 1, -1, 0, tileFlare.centre); //SWW
    castLightH(-1, 0, -1, 0, tileFlare.centre); //NWW
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


  //Cast light, iterating y across x
  /*void castLightH(float slopeA, float slopeB, int side, int width, sf::Vector2i start)
  {
      if(Controller::getLevel()->getPathing_cell(start.y,start.x) >= Pathing::WALL)
          return;

      float minSlope = min(slopeA,slopeB), maxSlope = max(slopeA,slopeB);
      bool inBlock = false;

      for(int i = 0, x = start.x; inView(x,start.y) && !inBlock; x+=side)
      {
          int first_y = start.y + minSlope*i - width;
          inBlock = (Controller::getLevel()->getPathing_cell(first_y,x)
              == Pathing::WALL);

          int free_cells = 0;
          for(int y = first_y; y <= start.y + maxSlope*i; y++)
          {

              if(inView(x,y))
              {
                  view_mask[y][x] = VISIBLE;

                  if(Controller::getLevel()->getPathing_cell(y,x) == Pathing::WALL)
                  {
                      if(!inBlock && y-1 >= first_y)
                      {
                          sf::Vector2i newStart(x,y-1);
                          float newSlope = side*((float)newStart.y-start.y)
                              /((float)newStart.x-start.x);
                          castLightH(min(minSlope,maxSlope),newSlope,side,max(0,free_cells-1),newStart);
                          inBlock = true;
                      }
                      free_cells = 0;
                  }
                  else
                  {
                      if(inBlock)
                      {
                          float newSlope = -side*((float)start.y-y)/((float)x-start.x);
                          minSlope = newSlope;
                          inBlock = false;
                          width = 0;
                      }
                      free_cells++;
                  }

              }
          }
          i++;
      }
  }*/

}
