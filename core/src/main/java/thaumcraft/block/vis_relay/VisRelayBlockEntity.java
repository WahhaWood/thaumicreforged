package thaumcraft.block.vis_relay;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.TileVisNode;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.api.wands.IWandable;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModSounds;

public class VisRelayBlockEntity extends TileVisNode implements IWandable {
   public byte color = -1;
   public static HashMap<Integer, WeakReference<VisRelayBlockEntity>> nearbyPlayers = new HashMap<>();
   protected Object beam1 = null;
   protected int pulse;
   public float pRed = 0.5F;
   public float pGreen = 0.5F;
   public float pBlue = 0.5F;
   public static final int[] colors = new int[]{16777086, 16727041, 37119, 40960, 15650047, 5592439};
   protected int px;
   protected int py;
   protected int pz;
   protected boolean parentLoaded = false;

   public VisRelayBlockEntity(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
      super(type, blockPos, blockState);
   }

   public VisRelayBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.VIS_RELAY.get(), blockPos, blockState);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (this.parentLoaded) {
         this.reconstructParentClient();
      }

      if (VisNetHandler.isNodeValid(this.getParent())) {
         TileVisNode parentNode = this.getParent().get();
         // TC4 ForgeDirection.UNKNOWN: a beam from the energized node starts at its very centre.
         Vec3 parentShift = parentNode instanceof VisRelayBlockEntity entity ? Vec3.atLowerCornerOf(entity.getFacing().getNormal()).scale(0.05) : Vec3.ZERO;
         Direction myFace = this.getFacing();
         Vec3 parentCenter = parentNode.getBlockPos().getCenter();
         Vec3 myCenter = this.getBlockPos().getCenter();
         this.beam1 = Thaumcraft.setup
            .beamPower(
               this.level,
               parentCenter.x - parentShift.x,
               parentCenter.y - parentShift.y,
               parentCenter.z - parentShift.z,
               myCenter.x - myFace.getStepX() * 0.05,
               myCenter.y - myFace.getStepY() * 0.05,
               myCenter.z - myFace.getStepZ() * 0.05,
               this.pRed,
               this.pGreen,
               this.pBlue,
               this.pulse > 0,
               this.beam1
            );
      }

      if (this.pRed < 1.0F) {
         this.pRed = Math.min(1.0F, this.pRed + 0.025F);
      }

      if (this.pGreen < 1.0F) {
         this.pGreen = Math.min(1.0F, this.pGreen + 0.025F);
      }

      if (this.pBlue < 1.0F) {
         this.pBlue = Math.min(1.0F, this.pBlue + 0.025F);
      }

      if (this.pulse > 0) {
         this.pulse--;
      }
   }

   /**
    * Регистрация игроков рядом — порт {@code TileVisRelay.func_145845_h} (1.7.10,
    * строки 96-111).
    *
    * <p>Ключ карты — {@code entityId} игрока ({@code func_145782_y}), а не
    * UUID: в 1.7.10 так и есть, и тип карты {@code HashMap<Integer, …>} в порте
    * поставлен верно.
    *
    * <p>Каждые 20 тиков ретранслятор ищет игроков в своей клетке, расширенной
    * на 5 блоков, и записывает ближайшего в {@link #nearbyPlayers}. Победителем
    * становится ретранслятор, у которого игрок ближе, — сравнение идёт по
    * {@code getDistanceSquared}, поэтому при равных значениях запись не
    * перезаписывается.
    *
    * <p>Раньше этого кода не было, и карта оставалась пустой всегда. Амулет
    * виса в порте не читал её вообще, так что вместе с {@code
    * ItemAmuletVis.curioTick} это была мёртвая пара: подпитывать амулет от
    * ретранслятора было нечем.
    */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      super.serverTick(level, pos, state);
      // TC4 BlockMetalDevice.getLightValue: a linked relay glows 10, a lone one 2.
      boolean connected = VisNetHandler.isNodeValid(this.getParent());
      if (state.hasProperty(VisRelayBlock.CONNECTED) && state.getValue(VisRelayBlock.CONNECTED) != connected) {
         level.setBlock(pos, state.setValue(VisRelayBlock.CONNECTED, connected), 2);
      }

      if (this.nodeCounter % 20 != 0) {
         return;
      }

      AABB box = new AABB(pos).inflate(5.0D, 5.0D, 5.0D);
      List<Player> players = level.getEntitiesOfClass(Player.class, box);

      for (Player player : players) {
         var current = nearbyPlayers.get(player.getId());
         if (current == null || current.get() == null
            || current.get().distanceToSqr(player.getX(), player.getY(), player.getZ())
               >= this.distanceToSqr(player.getX(), player.getY(), player.getZ())) {
            nearbyPlayers.put(player.getId(), new WeakReference<>(this));
         }
      }
   }

   private void reconstructParentClient() {
      if (this.px == 0 && this.py == 0 && this.pz == 0) {
         this.setParent(null);
      } else {
         BlockPos parentPos = this.worldPosition.offset(-this.px, -this.py, -this.pz);
         if (!this.level.isLoaded(parentPos)) {
            // The parent's chunk may reach the client after ours; TC4 gave up here and the beam stayed off until the next relink.
            return;
         }

         if (this.level.getBlockEntity(parentPos) instanceof TileVisNode node) {
            this.setParent(new WeakReference<>(node));
         }
      }

      this.parentLoaded = false;
      this.parentChanged();
   }

   /** TC4 relay attunement (wand or shard): only untuned relays or ones tuned alike link up. -1 is untuned. */
   public void tune(byte color) {
      this.color = color;
      this.removeThisNode();
      this.nodeRefresh = true;
      this.setChanged();
      this.markForUpdate();
      this.level.playSound(null, this.worldPosition, ModSounds.CRYSTAL.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
   }

   @Override
   public byte getAttunement() {
      return this.color;
   }

   @Override
   public int getRange() {
      return 8;
   }

   @Override
   public boolean isSource() {
      return false;
   }

   @Override
   public void parentChanged() {
      if (this.level != null && this.level.isClientSide) {
         this.level.getLightEngine().checkBlock(this.worldPosition);
      }
   }

   @Override
   public void setRemoved() {
      this.beam1 = null;
      super.setRemoved();
   }

   @Override
   public void triggerConsumeEffect(Aspect aspect) {
      this.addPulse(aspect);
   }

   protected void addPulse(Aspect aspect) {
      int c = -1;
      if (aspect == Aspect.AIR) {
         c = 0;
      } else if (aspect == Aspect.FIRE) {
         c = 1;
      } else if (aspect == Aspect.WATER) {
         c = 2;
      } else if (aspect == Aspect.EARTH) {
         c = 3;
      } else if (aspect == Aspect.ORDER) {
         c = 4;
      } else if (aspect == Aspect.ENTROPY) {
         c = 5;
      }

      if (c >= 0 && this.pulse == 0) {
         this.pulse = 5;
         this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 0, c);
      }
   }

   /** TC4 func_145842_c: event 0 is handled on both sides; the server's true is what sends it to clients at all. */
   @Override
   public boolean triggerEvent(int id, int type) {
      if (id != 0) {
         return super.triggerEvent(id, type);
      } else if (!this.level.isClientSide) {
         return true;
      } else if (type >= 0 && type < colors.length) {
         Color unused = new Color(colors[type]);
         this.pulse = 5;
         this.pRed = unused.getRed() / 255.0F;
         this.pGreen = unused.getGreen() / 255.0F;
         this.pBlue = unused.getBlue() / 255.0F;
         WeakReference _x = this.getParent();

         while (VisNetHandler.isNodeValid(_x)) {
            TileVisNode _xx = (TileVisNode)_x.get();
            if (!(_xx instanceof VisRelayBlockEntity _xxx) || _xxx.pulse != 0) {
               break;
            }

            _xxx.pRed = this.pRed;
            _xxx.pGreen = this.pGreen;
            _xxx.pBlue = this.pBlue;
            _xxx.pulse = 5;
            _x = _xx.getParent();
         }

         return true;
      } else {
         return true;
      }
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.color = compound.getByte("color");
      if (this.color >= colors.length) {
         this.color = -1;
      }

      this.px = compound.getByte("px");
      this.py = compound.getByte("py");
      this.pz = compound.getByte("pz");
      this.parentLoaded = true;
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      compound.putByte("color", this.color);
      if (VisNetHandler.isNodeValid(this.getParent())) {
         TileVisNode tile = this.getParent().get();
         compound.putByte("px", (byte)(this.worldPosition.getX() - tile.getBlockPos().getX()));
         compound.putByte("py", (byte)(this.worldPosition.getY() - tile.getBlockPos().getY()));
         compound.putByte("pz", (byte)(this.worldPosition.getZ() - tile.getBlockPos().getZ()));
      } else {
         compound.putByte("px", (byte)0);
         compound.putByte("py", (byte)0);
         compound.putByte("pz", (byte)0);
      }
   }

   /** TC4 TileVisRelay.onWandRightClick: the wand cycles the attunement through the six primals and back to none. */
   @Override
   public int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos blockPos, @NotNull Direction direction
   ) {
      if (!level.isClientSide) {
         this.tune((byte)(this.color >= colors.length - 1 ? -1 : this.color + 1));
      }

      // TC4 returned 0; 1 keeps the wand's focus from also firing at the relay.
      return 1;
   }

   @NotNull
   @Override
   public ItemStack onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      return ItemStack.EMPTY;
   }

   @Override
   public void onUsingWandTick(@NotNull ItemStack stack, @NotNull Player player, int count) {
   }

   @Override
   public void onWandStoppedUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player, int count) {
   }
}
