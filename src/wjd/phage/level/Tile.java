/*
 Copyright (C) 2012 William James Dyce

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IDynamic;
import wjd.amb.resources.ATextureManager;
import wjd.amb.resources.Tileset;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.amb.view.IVisible;
import wjd.amb.view.TilesetCanvas;
import wjd.math.Rect;
import wjd.math.V2;
import wjd.phage.unit.Unit;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class Tile implements IVisible, IDynamic
{
  /* CONSTANTS */
  public static final V2 SIZE = new V2(32, 32);
  public static final V2 HSIZE = SIZE.clone().scale(0.5f);
  public static final V2 ISIZE = SIZE.clone().inv();
  
  public static final Colour C_WALL = new Colour(97, 0, 21);
  public static final Colour C_FOG = new Colour(0, 0, 0, 128);
  public static final Colour C_FOG_WALL = C_WALL.clone().avg(C_FOG);

  /* NESTING */
  public static enum ETerrain { FLOOR, WALL }
  public static enum EVisibility { UNEXPLORED, UNSEEN, VISIBLE }
  
  /* RESOURCES */
  private static Tileset fog;
  public static void getResourceHandles(ATextureManager textureManager)
  {
    fog = textureManager.getTileset("fog");
  }

  /* ATTRIBUTES */
  public final TileGrid grid;
  public final V2 grid_position, pixel_position;
  public final Rect pixel_area;
  
  private Unit unit = null, unit_inbound = null;
  private Infection infection = new Infection(this);
  
  // terrain type and type neighbourhood
  private ETerrain terrain = ETerrain.FLOOR;
  private byte[] terrain_neighbours = { 0, 0, 0, 0 };
  // visibility type and type neighbourhood
  private EVisibility visibility = EVisibility.UNEXPLORED;
  private byte visibility_neighbours = 15; // 1 + 2 + 4 + 8
  private TilesetCanvas fog_stamp;
  
  /* METHODS */
  
  // constructors
  public Tile(int row, int col, ETerrain terrain_, TileGrid grid_)
  {
    this.grid = grid_;
    
    grid_position = new V2(col, row);
    pixel_position = grid_position.clone().scale(SIZE);
    pixel_area = new Rect(pixel_position, SIZE);
    this.terrain = terrain_;
    fog_stamp = new TilesetCanvas(fog, pixel_area);

  }
  
  public Tile(ObjectInputStream in, TileGrid grid) throws IOException, ClassNotFoundException
  {
    this.grid = grid;
    
    // retrieve grid position and deduce pixel position and area
    grid_position = (V2)in.readObject();
    pixel_position = grid_position.clone().scale(SIZE);
    pixel_area = new Rect(pixel_position, SIZE).scale(1.1f);

    terrain = (ETerrain)in.readObject();
    
    infection = new Infection(in, this);
    
    // read unit if unit is present to be read
    if((Boolean)in.readObject()) 
      unit = Unit.load(this, in);
    
    fog_stamp = new TilesetCanvas(fog, pixel_area);
  }

  // accessors
  public Unit getUnit()
  {
    return (unit != null) ? unit : unit_inbound;
  }
  
  public ETerrain getType()
  {
    return terrain;
  }
  
  public boolean isPathable()
  {
    return (terrain == ETerrain.FLOOR && unit == null && unit_inbound == null);
  }
  
  public Infection getInfection()
  {
    return infection;
  }
  
  // mutators
  
  public void refreshVisibilityNeighbours()
  {
   // We'll only consider the 4 sides of the Tile and whether they visible too
   
   visibility_neighbours = 0;
   V2 pos = new V2().reset(grid_position);
    
    /*  -x-
        -X- = 1 (binary 0001)
        ---                     */
    
    pos.y--;
    if(!grid.validGridPos(pos) || grid.gridToTile(pos).visibility == visibility)
      visibility_neighbours += 1;
      
    /*  ---
        -Xx = 2 (binary 0010)
        ---                     */
    
    pos.x++; pos.y++;
    if(!grid.validGridPos(pos) || grid.gridToTile(pos).visibility == visibility)
      visibility_neighbours += 2;
    
    /*  ---
        -X- = 4 (binary 0100)
        -x-                     */
    
    pos.x--; pos.y++;
    if(!grid.validGridPos(pos) || grid.gridToTile(pos).visibility == visibility)
      visibility_neighbours += 4;
    
    /*  ---
        xX- = 8 (binary 1000)
        ---                     */
    
    pos.x--; pos.y--;
    if(!grid.validGridPos(pos) || grid.gridToTile(pos).visibility == visibility)
      visibility_neighbours += 8;

    fog_stamp.tile_i = (int)visibility_neighbours;
  }
  
  public void refreshTerrainNeighbours()
  {
    /* Each of the 4 corners is given a seperate hash value between 0 and 4. We
    only pay attention to the corners if the two adjascent sides are equal to
    the center:

    |--|    |-x|
    |X-| == |X-| == 0 (binary 00)

    |x-|    |xx|
    |X-| == |X-| == 1 (binary 01)

    |--|    |-x|
    |Xx| == |Xx| == 2 (binary 10)

    |xx|
    |Xx| == 4 (binary 11)

    Corner are evaluated in this order:
    0|x-| 1|--| 2|-x| 3|--|
     |--|  |x-|  |--|  |-x|
    */

    V2 pos = new V2();
    
    for(int d_col = -1, corner = 0; d_col < 2; d_col += 2)
    for(int d_row = -1; d_row < 2; d_row += 2, corner++)
    {
      // reset hash
      terrain_neighbours[corner] = 0;
      
      // delta along the vertical axis
      pos.xy(grid_position.x, grid_position.y + d_row);
      if(grid.validGridPos(pos) && grid.gridToTile(pos).terrain == terrain)
        terrain_neighbours[corner] += 1;
      
      // delta along the horizontal axis
      pos.xy(grid_position.x + d_col, grid_position.y);
      if(grid.validGridPos(pos) && grid.gridToTile(pos).terrain == terrain)
        terrain_neighbours[corner] += 2;
      
      // delta along both axes if both sides are of the same type as the center
      pos.xy(grid_position.x + d_col, grid_position.y + d_row);
      if(terrain_neighbours[corner] == 3 
      && grid.validGridPos(pos) && grid.gridToTile(pos).terrain == terrain)
        terrain_neighbours[corner] = 4;
    }
  }
  
  public final void setTerrain(ETerrain terrain_)
  {
    // reset the type
    this.terrain = terrain_;
    if(terrain_ == ETerrain.WALL)
      unit = null;
    if(terrain_ != ETerrain.FLOOR)
      infection.empty();
    
    // recalculate the hash
    refreshTerrainNeighbours();
    Iterable<Tile> n = grid.getNeighbours(this, true);
    for(Tile t : n)
      t.refreshTerrainNeighbours();
  }
  
  public final void setVisibility(EVisibility visibility_)
  {
    this.visibility = visibility_;
    refreshVisibilityNeighbours();
    
    Iterable<Tile> n = grid.getNeighbours(this, true);
    for(Tile t : n)
      t.refreshVisibilityNeighbours();
  }
  
  public final void setUnit(Unit new_unit)
  {
    if(unit == null || new_unit == null)
      unit = new_unit;
  }

  public final boolean unitStartEnter(Unit u)
  {
    // tile cannot be entered while someone else is present, entering or leaving
    if(unit != null || unit_inbound != null)
      return false;
    
    // the supplicant is now the inbound unit
    unit_inbound = u;
    return true;
  }
  
  public void save(ObjectOutputStream out) throws IOException 
  {
    // don't write pixel position or area, as these can be deduced
    out.writeObject(grid_position);

    out.writeObject(terrain);
    
    infection.save(out);
  
    // write a boolean to signify if unit is present or not
    out.writeObject(unit != null);
    if(unit != null)
      unit.save(out);
    
    // don't write the grid, or we'll end up with a recursion loop!
  }

  /* OVERRIDES -- IDYNAMIC */
  @Override
  public void render(ICanvas canvas)
  {
    // walls
    if(terrain == ETerrain.WALL)
    {
      canvas.setColour((visibility == EVisibility.VISIBLE) ? C_WALL : C_FOG_WALL);
      canvas.box(pixel_area, true);
    }
    

    if(visibility == EVisibility.VISIBLE)
    {
      // units (optional)
      if (unit != null)
        unit.render(canvas);
      if (unit_inbound != null)
        unit_inbound.render(canvas);

      // infection (optional)
      infection.render(canvas);
    }
    
    // black mask
    else
    {
      if(visibility_neighbours < 15 && terrain != ETerrain.WALL)
      {
        fog_stamp.render(canvas);
      }
      else
      {
        canvas.setColour(C_FOG);
        canvas.box(pixel_area, true);
      }
    }
  }
  
  /* OVERRIDES -- OBJECT */
  @Override
  public String toString()
  {
    return terrain + " at " + grid_position + (unit == null ? "" : " contains " + unit);
  }
  
  /* IMPLEMENTS -- IDYNAMIC */
 
  @Override
  public EUpdateResult update(int t_delta)
  {
    // update the unit if there is one
    if(unit != null)
    {
      EUpdateResult result = unit.update(t_delta);
      switch(result)
      {
        case DELETE_ME:
          unit = null;
          break;
          
        case REPLACE_ME:
          unit = unit.getReplacement();
          break;
      }
    }
    
    // update the inbound unit if there is one
    if(unit_inbound != null)
    {
      EUpdateResult result = unit_inbound.update(t_delta);
      
      switch(result)
      {
        case MOVE_ME:
          unit = unit_inbound;
          // break left out intentionally
          
        case DELETE_ME:
        case CANCEL:
          unit_inbound = null;
          break;
      }
    }
    
    // update the infection
    infection.update(t_delta);
    
    // all clear
    return EUpdateResult.CONTINUE;
  }
}
