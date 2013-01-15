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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Random;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IDynamic;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.amb.view.IVisible;
import wjd.util.BoundedValue;
import wjd.util.Timer;


/**
 *
 * @author wdyce
 * @since Jan 2, 2013
 */
public class Infection extends BoundedValue implements IDynamic, IVisible
{
  /* CONSTANTS */
  public static final int PARTICLE_MAX = 5;
  public static final float PARTICLE_SIZE = 5.0f;
  public static final float PARTICLE_MIN_ZOOM = 0.0f;
  
  public static final int DISPERSION_PERIOD = 300;         // ms
  public static final float DISPERSION_SPEED = 0.9f;       // fraction
  
  public static final boolean DO_DECAY = false;
  public static final int DECAY_PERIOD = 6000;             // ms
  public static final float DECAY_SPEED = 0.1f;            // fraction
  
  public static final int MOVE_PERIOD = 1000;          // ms
  public static final float MOVE_PERIOD_VAR = 0.5f;        // fraction
  
  public static final float CONCENTRATION_MIN = 0.09f;     // fraction
  
  
  public static final Colour C = new Colour(20, 206, 50);
  
  /* CLASS ATTRIBUTES */
  private static Random r = new Random();
  
  /* ATTRIBUTES */
  private Tile container;
  private Timer dispersion_timer = new Timer(DISPERSION_PERIOD);
  private Timer decay_timer = new Timer(DECAY_PERIOD);
  private Timer move_timer = new Timer(MOVE_PERIOD);
  
  private long r_seed;

  
  /* METHODS */
  
  
  // constructors
  
  public Infection(Tile container_)
  {
    super(1.0f);
    
    this.container = container_;
    r_seed = r.nextLong();
    
  }

  public Infection(ObjectInputStream in, Tile container_) throws IOException
  {
    super(1.0f);
    balance(in.readFloat());
    
    this.container = container_;
    r_seed = r.nextLong();
  }
  
  
  // io
  
  void save(ObjectOutputStream out) throws IOException
  {
    out.writeFloat(balance());
  }
  
  /* IMPLEMENTS -- IVISIBLE */
  
  @Override
  public void render(ICanvas canvas)
  {
    float zoom = canvas.getCamera().getZoom();
    if(!isEmpty() && zoom > PARTICLE_MIN_ZOOM)
    {
      // static random seed
      r.setSeed(r_seed);
      
      // draw virus in black
      canvas.setColour(C);
      
      // probability of a viral particle being present and number present
      float p_virus = balance() * zoom;
      int n_virus =  (int)(p_virus * (float)PARTICLE_MAX);
      
      // more than one virus -- draw always
      if(n_virus >= 1) for(int i = 0; i < n_virus; i++)
        renderParticle(canvas);
      
      // less than one virus -- draw only sometimes
      else if(r.nextDouble() > p_virus)
        renderParticle(canvas);
    }
  }
  
  /* IMPLEMENTS -- IDYNAMIC */
  
  @Override
  public EUpdateResult update(int t_delta)
  {
    // spread infection
    if(dispersion_timer.update(t_delta) == EUpdateResult.FINISHED)
      virusDisperse();
    
    // destroy infection
    if(DO_DECAY && decay_timer.update(t_delta) == EUpdateResult.FINISHED)
      virusDecay();
    
    // move particles
    if(move_timer.update(t_delta) == EUpdateResult.FINISHED)
    {
      r_seed = r.nextLong();
       move_timer.balance((float)(Math.random() * MOVE_PERIOD_VAR * MOVE_PERIOD));
    }
    
    // nothing to report
    return EUpdateResult.CONTINUE;
  }
  
  /* SUBROUTINES */
  
  private void renderParticle(ICanvas canvas)
  {
    // use pixel_position as a local variable
    container.pixel_position.add(
      (float)(PARTICLE_SIZE+r.nextDouble() * (Tile.SIZE.x-2*PARTICLE_SIZE)), 
      (float)(PARTICLE_SIZE+r.nextDouble() * (Tile.SIZE.y-2*PARTICLE_SIZE)));
    canvas.circle(container.pixel_position, PARTICLE_SIZE, true);
    // set it back to its original value afterwards!
    container.pixel_position.xy(container.pixel_area.x, container.pixel_area.y);
  }
  
  private void virusDecay()
  {
    // some of the viral particles are destroyed...
    tryWithdrawPercent(DECAY_SPEED);
    if(balance() < CONCENTRATION_MIN)
      empty();
  }
  
  private void virusDisperse()
  {
    if(balance() < CONCENTRATION_MIN)
      return;
    
    // disperse infection over neighbours
    List<Tile> neighbours = container.grid.getNeighbours(container, true);
    float dispersion = tryWithdrawPercent(DISPERSION_SPEED);
    float dispersion_per_tile = dispersion / neighbours.size();
    for(Tile t : neighbours)
      if(t.getType() == Tile.ETerrain.FLOOR)
        dispersion -= t.getInfection().tryDeposit(dispersion_per_tile);
    
    // return whatever is left
    tryDeposit(dispersion);
  }
}
