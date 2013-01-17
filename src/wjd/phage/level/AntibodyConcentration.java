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

import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.math.V2;

/**
 *
 * @author wdyce
 * @since Jan 17, 2013
 */
public class AntibodyConcentration extends Concentration
{
  /* CONSTANTS */
  private static V2[] OFFSET = { new V2(0, -PARTICLE_SIZE), 
                                new V2(PARTICLE_SIZE, PARTICLE_SIZE), 
                                new V2(-PARTICLE_SIZE, PARTICLE_SIZE) };
  public static final Colour C = new Colour(20, 206, 50);
  
  private static V2 pos = new V2();
  
  
  /* METHODS */
  
  // constructors
  
  public AntibodyConcentration(Tile container_)
  {
    super(container_);
  }
  
  /* IMPLEMENTS CONCENTRATION */
  
  @Override
  public EType getType()
  {
    return EType.ANTIBODY;
  }
  
  @Override
  protected void renderParticle(ICanvas canvas)
  {
    // use pixel_position as a local variable
    container.pixel_position.add(
      (float)(PARTICLE_SIZE + r.nextDouble() * (Tile.SIZE.x-2*PARTICLE_SIZE)), 
      (float)(PARTICLE_SIZE + r.nextDouble() * (Tile.SIZE.y-2*PARTICLE_SIZE)));
    
    canvas.setLineWidth(2.0f);
    for(int i = 0; i < 3; i++)
    {
      pos.reset(container.pixel_position).add(OFFSET[i]);
      canvas.line(container.pixel_position, pos);
    }
    
    // set it back to its original value afterwards!
    container.pixel_position.xy(container.pixel_area.x, container.pixel_area.y);
  }
  
  @Override
  public Colour getColour()
  {
    return C;
  }
  
  @Override
  public boolean doesDecay()
  {
    return false;
  }
}
