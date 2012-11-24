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
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.amb.view.IVisible;
import wjd.math.Rect;
import wjd.math.V2;
import wjd.phage.unit.Unit;
import wjd.util.BoundedValue;

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
  public static final int MAX_PARTICLES = 5;
  public static final float PARTICLE_SIZE = 1.8f;
  public static final float PARTICLE_MIN_ZOOM = 0.0f;


  /* NESTING */
  public static enum EType
  {

    FLOOR,
    WALL
  }

  /* ATTRIBUTES */
  public final TileGrid grid;
  public final V2 grid_position, pixel_position;
  private final Rect pixel_area;
  private EType type;
  private Unit unit = null;
  private BoundedValue infection = new BoundedValue(1.0f);

  /* METHODS */
  
  // constructors
  public Tile(int row, int col, EType type, TileGrid grid)
  {
    grid_position = new V2(col, row);
    pixel_position = grid_position.clone().scale(SIZE);
    pixel_area = new Rect(pixel_position, SIZE);
    this.type = type;
    this.grid = grid;
  }
  
  public Tile(ObjectInputStream in, TileGrid grid) throws IOException, ClassNotFoundException
  {
    // retrieve grid position and deduce pixel position and area
    grid_position = (V2)in.readObject();
    pixel_position = grid_position.clone().scale(SIZE);
    pixel_area = new Rect(pixel_position, SIZE);

    type = (EType)in.readObject();
    
    // read unit if unit is present to be read
    if((Boolean)in.readObject()) 
      unit = new Unit(this, in);
    
    this.grid = grid;
  }

  // accessors
  public Unit getUnit()
  {
    return unit;
  }
  
  public EType getType()
  {
    return type;
  }
  
  public BoundedValue getInfection()
  {
    return infection;
  }
  
  // mutators
  public void setType(EType type)
  {
    this.type = type;
    if(type == EType.WALL)
      unit = null;
    if(type != EType.FLOOR)
      infection.empty();
  }

  public void setUnit(Unit unit)
  {
    this.unit = unit;
  }
  
  
  public void save(ObjectOutputStream out) throws IOException 
  {
    // don't write pixel position or area, as these can be deduced
    out.writeObject(grid_position);

    out.writeObject(type);
    
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
    // setup context
    canvas.setColour(type == EType.FLOOR ? Colour.YELLOW : Colour.BLUE);

    // background
    canvas.box(pixel_area, true);
    
    // unit (optional)
    if (unit != null)
      unit.render(canvas);
    
    // infection (optional)
    canvas.setColour(Colour.GREEN);
    float zoom = canvas.getCamera().getZoom();
    if(!infection.isEmpty() && zoom > PARTICLE_MIN_ZOOM)
    {
      // probability of a viral particle being present and number present
      float p_virus = infection.value() * zoom;
      int n_virus =  (int)(p_virus * (float)MAX_PARTICLES);
      
      // more than one virus -- draw always
      if(n_virus >= 1) for(int i = 0; i < n_virus; i++)
        renderParticle(canvas);
      
      // less than one virus -- draw only sometimes
      if(Math.random() > p_virus)
        renderParticle(canvas);
    }
  }
  
  /* OVERRIDES -- OBJECT */
  @Override
  public String toString()
  {
    return type + " at " + grid_position + (unit == null ? "" : " contains " + unit);
  }
  
  /* IMPLEMENTS -- IDYNAMIC */
 
  @Override
  public EUpdateResult update(int t_delta)
  {
    // update the unit if there is one
    if(unit != null)
    {
      EUpdateResult result = unit.update(t_delta);
      // delete if required
      if(result == EUpdateResult.DELETE_ME)
        unit = null;
    }

    // spread infection
    
    // all clear
    return EUpdateResult.CONTINUE;
  }
  
  /* SUBROUTINES */
  
  private void renderParticle(ICanvas canvas)
  {
    // use pixel_position as a local variable
    pixel_position.add(
      (float)(PARTICLE_SIZE+Math.random() * (SIZE.x-2*PARTICLE_SIZE)), 
      (float)(PARTICLE_SIZE+Math.random() * (SIZE.y-2*PARTICLE_SIZE)));
    canvas.circle(pixel_position, PARTICLE_SIZE, true);
    // set it back to its original value afterwards!
    pixel_position.xy(pixel_area.x, pixel_area.y);
  }
}
