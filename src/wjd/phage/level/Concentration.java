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
public abstract class Concentration extends BoundedValue implements IDynamic, IVisible
{
  /* NESTING */
  
  public static enum EType 
  { 
    VIRUS(ViralConcentration.C), 
    ANTIBODY(AntibodyConcentration.C);
    
        
    public final Colour colour;
    
    private EType(Colour colour_)
    {
      this.colour = colour_;
    }
  };
  
  /* CLASS NAMESPACE FUNCTIONS */
  
  public static Concentration load(Tile tile,  ObjectInputStream in) 
  throws IOException, ClassNotFoundException
  {
    // create an object of the correct type
    EType t = (EType )in.readObject();
    Concentration result = fromType(t, tile);
    result.balance(in.readFloat());
    return result;
    
  }
  
  public static Concentration fromType(EType type, Tile tile)
  {
    switch(type)
    {
      case VIRUS:
        return new ViralConcentration(tile);
        
      case ANTIBODY:
        return new AntibodyConcentration(tile);
        
        
        // ... etc
        
        
      default:
        return null;
    }
  }
  
  /* CONSTANTS */
  public static final int PARTICLE_MAX = 5;
  public static final float PARTICLE_SIZE = 5.0f;
  public static final float PARTICLE_MIN_ZOOM = 0.0f;
  
  public static final int DISPERSION_PERIOD = 450;         // ms
  public static final float DISPERSION_SPEED = 0.99f;       // fraction
  
  public static final int DECAY_PERIOD = 6000;             // ms
  public static final float DECAY_SPEED = 0.1f;            // fraction
  
  public static final int MOVE_PERIOD = 1000;          // ms
  public static final float MOVE_PERIOD_VAR = 0.5f;        // fraction
  
  public static final float CONCENTRATION_MIN = 0.09f;     // fraction
  
  /* CLASS ATTRIBUTES */
  protected static Random r = new Random();
  
  /* ATTRIBUTES */
  protected Tile container;
  private Timer dispersion_timer = new Timer(DISPERSION_PERIOD);
  private Timer decay_timer = new Timer(DECAY_PERIOD);
  private Timer move_timer = new Timer(MOVE_PERIOD);
  
  private long r_seed;

  
  /* METHODS */
  
  
  // constructors
  
  public Concentration(Tile container_)
  {
    super(1.0f);
    
    this.container = container_;
    r_seed = r.nextLong();
    
  }
  
  
  // io
  
  void save(ObjectOutputStream out) throws IOException
  {
    out.writeObject(getType());
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
      canvas.setColour(getColour());
      
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
      disperse();
    
    // destroy infection
    if(doesDecay() && decay_timer.update(t_delta) == EUpdateResult.FINISHED)
      decay();
    
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
  
  protected abstract void renderParticle(ICanvas canvas);
  
  private void decay()
  {
    // some of the viral particles are destroyed...
    tryWithdrawPercent(DECAY_SPEED);
    if(balance() < CONCENTRATION_MIN)
      empty();
  }
  
  private void disperse()
  {
    if(balance() < CONCENTRATION_MIN)
      return;
    
    // disperse infection over neighbours
    List<Tile> neighbours = container.grid.getNeighbours(container, true);
    float dispersion = tryWithdrawPercent(DISPERSION_SPEED);
    float dispersion_per_tile = dispersion / neighbours.size();
    for(Tile t : neighbours)
      if(t.getType() == Tile.ETerrain.FLOOR)
        dispersion -= t.getConcentration(getType()).tryDeposit(dispersion_per_tile);
    
    // return whatever is left
    tryDeposit(dispersion);
  }
  
  /* INTERFACE */
  
  public abstract EType getType();
  
  public abstract Colour getColour();
  
  public abstract boolean doesDecay();
}
